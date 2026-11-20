package com.devil.livetogather

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

data class RoomLevelUpEvent(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhoto: String,
    val newLevel: Int,
    val isRoomStayLevel: Boolean,
    val xpBonus: Long,
    val timestamp: Long
)

object RoomLevelUpEngine {
    const val ROOM_STAY_LEVEL_INTERVAL_SECONDS = 300L // Every 5 minutes in a room
    const val ROOM_STAY_XP_PER_5_MIN = 100L
    const val GLOBAL_WEALTH_XP_PER_5_MIN = 25L
    const val POPUP_DISPLAY_DURATION_MS = 2800L // Auto-dismiss cleanly in ~2.8 seconds

    fun sanitizeRoomKey(roomId: String): String {
        return roomId.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "default_room" }
    }

    fun calculateRoomLevelFromXp(roomXp: Long): Int {
        return ((roomXp.coerceAtLeast(0L) / ROOM_STAY_XP_PER_5_MIN).toInt() + 1).coerceIn(1, 100)
    }

    fun isLevelUpSystemComment(comment: LiveRoomComment): Boolean {
        return comment.systemCommentType == "room_level_up" ||
            comment.systemCommentType == "wealth_level_up" ||
            comment.message.contains("has been leveled up", ignoreCase = true)
    }

    fun toEvent(comment: LiveRoomComment): RoomLevelUpEvent {
        val lvl = if (comment.roomLevelValue > 0) comment.roomLevelValue else comment.level.coerceAtLeast(1)
        return RoomLevelUpEvent(
            id = comment.id.ifBlank { "${comment.userId}_${comment.timestamp}" },
            userId = comment.userId,
            userName = comment.userName.ifBlank { "Member" },
            userPhoto = comment.userPhoto,
            newLevel = lvl,
            isRoomStayLevel = comment.systemCommentType != "wealth_level_up",
            xpBonus = if (comment.roomXpBonus > 0L) comment.roomXpBonus else ROOM_STAY_XP_PER_5_MIN,
            timestamp = comment.timestamp
        )
    }

    /**
     * Broadcasts a real-time room-wide level-up announcement to `rooms/{roomId}/live_comments`.
     * Every active user in the room receives it via their live_comments snapshot listener,
     * displays the 2.8s celebration popup first, and then shows the permanent System Comment below.
     */
    fun dispatchRoomLevelUpComment(
        db: FirebaseFirestore,
        roomId: String,
        currentUser: UserProfile,
        newLevel: Int,
        isRoomStayLevel: Boolean,
        xpBonus: Long = ROOM_STAY_XP_PER_5_MIN
    ) {
        if (roomId.isBlank() || currentUser.uid.isBlank() || newLevel < 2) return
        val commentId = "lvlup_${currentUser.uid}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val safeName = currentUser.name.ifBlank { "Member" }
        val tierInfo = LevelBadgeSystem.getLevelTierInfo(newLevel)
        val systemType = if (isRoomStayLevel) "room_level_up" else "wealth_level_up"
        val msg = if (isRoomStayLevel) {
            "$safeName has been leveled up to Room Lv.$newLevel in the room!"
        } else {
            "$safeName has been leveled up to Lv.$newLevel in the room!"
        }

        val comment = LiveRoomComment(
            id = commentId,
            roomId = roomId,
            userId = currentUser.uid,
            userName = safeName,
            userPhoto = currentUser.photo,
            tier = tierInfo.tierName,
            message = msg,
            timestamp = System.currentTimeMillis(),
            vipLevel = currentUser.getVipLevel(),
            isFounder = currentUser.isFounder(),
            level = newLevel,
            systemCommentType = systemType,
            roomLevelValue = newLevel,
            roomXpBonus = xpBonus
        )

        db.collection("rooms").document(roomId)
            .collection("live_comments").document(commentId)
            .set(comment)
            .addOnFailureListener { e ->
                Log.w("RoomLevelUpEngine", "Failed to dispatch room level-up comment: ${e.message}")
            }
    }

    /**
     * Records a 10-second live stay tick for a specific room in real-time Firestore.
     * Every 5 minutes (300 seconds) spent inside that room:
     * 1. Grants +100 per-room XP -> upgrades Per-Room Level in Firestore (`roomXpMap`, `roomLevelsMap`).
     * 2. Grants +25 Global Wealth XP -> updates `wealthXp`, `wealthLevel`, `level`, `activeWealthBadgeId`.
     * 3. Dispatches the real-time Room Level-Up Celebration Popup + System Comment for everyone in the room.
     */
    fun recordRoomStayTick(
        context: android.content.Context,
        db: FirebaseFirestore,
        roomId: String,
        currentUser: UserProfile,
        newRoomStaySeconds: Long,
        onWealthStatsUpdated: (wealthXp: Long, wealthLevel: Int, roomLevel: Int) -> Unit = { _, _, _ -> }
    ) {
        if (roomId.isBlank() || currentUser.uid.isBlank()) return
        val safeKey = sanitizeRoomKey(roomId)
        val userRef = db.collection("users").document(currentUser.uid)

        // Persist global + per-room stay seconds atomically in real-time Firestore
        userRef.update(
            mapOf(
                "totalRoomTimeSeconds" to FieldValue.increment(10L),
                "roomStaySecondsMap.$safeKey" to FieldValue.increment(10L)
            )
        ).addOnFailureListener {
            // Fallback for older user docs without roomStaySecondsMap initialized
            userRef.update("totalRoomTimeSeconds", FieldValue.increment(10L))
        }

        // Trigger Per-Room XP & Room Level-Up every 5 minutes (300 seconds) spent in this room
        if (newRoomStaySeconds > 0L && newRoomStaySeconds % ROOM_STAY_LEVEL_INTERVAL_SECONDS == 0L) {
            userRef.get().addOnSuccessListener { snap ->
                if (snap == null || !snap.exists()) return@addOnSuccessListener

                @Suppress("UNCHECKED_CAST")
                val roomXpMap = snap.get("roomXpMap") as? Map<String, Any>
                @Suppress("UNCHECKED_CAST")
                val roomLevelsMap = snap.get("roomLevelsMap") as? Map<String, Any>

                val liveRoomXp = (roomXpMap?.get(safeKey) as? Number)?.toLong() ?: 0L
                val liveRoomLevel = (roomLevelsMap?.get(safeKey) as? Number)?.toInt()
                    ?: calculateRoomLevelFromXp(liveRoomXp)
                val updatedRoomXp = liveRoomXp + ROOM_STAY_XP_PER_5_MIN
                val updatedRoomLevel = calculateRoomLevelFromXp(updatedRoomXp)

                val liveWealthXp = snap.getLong("wealthXp") ?: currentUser.wealthXp
                val liveWealthLvl = maxOf(
                    (snap.getLong("wealthLevel") ?: 1L).toInt(),
                    LevelBadgeSystem.calculateLevelFromXp(liveWealthXp)
                )
                val updatedWealthXp = liveWealthXp + GLOBAL_WEALTH_XP_PER_5_MIN
                val updatedWealthLvl = maxOf(
                    liveWealthLvl,
                    LevelBadgeSystem.calculateLevelFromXp(updatedWealthXp)
                )
                val isGlobalLevelUp = updatedWealthLvl > liveWealthLvl
                val isRoomLevelUp = updatedRoomLevel > liveRoomLevel

                val updates = mutableMapOf<String, Any>(
                    "roomXpMap.$safeKey" to updatedRoomXp,
                    "roomLevelsMap.$safeKey" to updatedRoomLevel,
                    "wealthXp" to updatedWealthXp,
                    "wealthLevel" to updatedWealthLvl,
                    "level" to updatedWealthLvl,
                    "activeWealthBadgeId" to LevelBadgeSystem.getTierBadgeId(updatedWealthLvl)
                )

                if (isGlobalLevelUp) {
                    updates["diamonds"] = FieldValue.increment(5L)
                    val notifId = UUID.randomUUID().toString()
                    val notifDoc = mapOf(
                        "id" to notifId,
                        "type" to "level_up",
                        "title" to "🎉 Level Up! Lv.$updatedWealthLvl",
                        "message" to "Congratulations! You reached Wealth Level $updatedWealthLvl (${LevelBadgeSystem.getLevelTierInfo(updatedWealthLvl).tierName})! +5 Bonus Diamonds unlocked.",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false,
                        "senderName" to "LiveTogather Official"
                    )
                    userRef.collection("notifications").document(notifId).set(notifDoc)
                }

                userRef.update(updates).addOnSuccessListener {
                    onWealthStatsUpdated(updatedWealthXp, updatedWealthLvl, updatedRoomLevel)

                    if (isRoomLevelUp) {
                        dispatchRoomLevelUpComment(
                            db = db,
                            roomId = roomId,
                            currentUser = currentUser,
                            newLevel = updatedRoomLevel,
                            isRoomStayLevel = true,
                            xpBonus = ROOM_STAY_XP_PER_5_MIN
                        )
                    }

                    if (isGlobalLevelUp) {
                        val updatedProfile = currentUser.copy(
                            wealthXp = updatedWealthXp,
                            wealthLevel = updatedWealthLvl
                        )
                        ProgressionAutomationService.evaluateAndDispatchMilestones(context, updatedProfile, db)
                    }
                }
            }
        }
    }
}

/**
 * Luxury 2.8-second floating in-room Level-Up Celebration Popup with:
 * - Animated golden halo, diagonal metallic shimmer sweep, and twinkling stars
 * - Real user avatar in glowing gold ring + crisp HD transparent Tier Badge crest
 * - Prominent username mention & clear level-up message
 * - Auto-dismisses cleanly after ~2.8 seconds, then invokes `onFinished` so the System Comment reveals below
 */
@Composable
fun RoomLevelUpCelebrationPopup(
    event: RoomLevelUpEvent,
    isCurrentUser: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember(event.id) { mutableStateOf(false) }

    LaunchedEffect(event.id) {
        visible = true
        delay(RoomLevelUpEngine.POPUP_DISPLAY_DURATION_MS)
        visible = false
        delay(320L)
        onFinished()
    }

    val tierInfo = remember(event.newLevel) { LevelBadgeSystem.getLevelTierInfo(event.newLevel) }
    val accentPrimary = tierInfo.gradientColors.firstOrNull() ?: Color(0xFFFFD700)
    val accentSecondary = tierInfo.gradientColors.lastOrNull() ?: Color(0xFFFF9800)

    val infiniteTransition = rememberInfiniteTransition(label = "room_lvl_up_shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + scaleIn(
            initialScale = 0.84f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(240)),
        exit = slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = tween(280)
        ) + scaleOut(targetScale = 0.92f, animationSpec = tween(280)) + fadeOut(tween(260)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 344.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer royal golden aura glow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.22f * sparklePulse),
                                accentSecondary.copy(alpha = 0.26f * sparklePulse),
                                Color(0xFFFFD700).copy(alpha = 0.22f * sparklePulse)
                            )
                        )
                    )
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                shadowElevation = 16.dp,
                border = BorderStroke(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF3B0),
                            Color(0xFFFFD700),
                            accentSecondary,
                            Color(0xFFFFF3B0)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xF51A0F2E),
                                    Color(0xF52D164B),
                                    Color(0xF5160C26)
                                )
                            )
                        )
                ) {
                    // Animated diagonal golden shimmer & twinkling sparkle stars overlay
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val sweepX = size.width * shimmerProgress
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x38FFF5C0),
                                    Color(0x18FFFFFF),
                                    Color.Transparent
                                ),
                                start = Offset(sweepX - 70f, 0f),
                                end = Offset(sweepX + 70f, size.height)
                            )
                        )

                        // Soft glowing star particles along top/bottom rim
                        val starPositions = listOf(
                            Offset(size.width * 0.14f, size.height * 0.18f),
                            Offset(size.width * 0.48f, size.height * 0.12f),
                            Offset(size.width * 0.80f, size.height * 0.16f),
                            Offset(size.width * 0.28f, size.height * 0.88f),
                            Offset(size.width * 0.72f, size.height * 0.86f)
                        )
                        starPositions.forEachIndexed { idx, pt ->
                            val r = if (idx % 2 == 0) 3.6f * sparklePulse else 2.6f * (1.35f - sparklePulse * 0.4f)
                            drawCircle(
                                color = Color(0xFFFFE57F).copy(alpha = 0.85f * sparklePulse),
                                radius = r.coerceAtLeast(1.5f),
                                center = pt
                            )
                            val angle = idx * 0.7f
                            drawLine(
                                color = Color.White.copy(alpha = 0.55f * sparklePulse),
                                start = Offset(pt.x - cos(angle) * r * 2f, pt.y - sin(angle) * r * 2f),
                                end = Offset(pt.x + cos(angle) * r * 2f, pt.y + sin(angle) * r * 2f),
                                strokeWidth = 1.2f
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Real User Avatar in Luminous Golden Ring
                        Box(
                            modifier = Modifier.size(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            colors = listOf(
                                                Color(0xFFFFF59D),
                                                Color(0xFFFFD700),
                                                accentSecondary,
                                                Color(0xFFFFF59D)
                                            )
                                        )
                                    )
                                    .padding(2.dp)
                            ) {
                                if (event.userPhoto.isNotBlank()) {
                                    AsyncImage(
                                        model = event.userPhoto,
                                        contentDescription = event.userName,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(accentPrimary, accentSecondary))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = event.userName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                            // Sparkle crown dot at bottom-right of avatar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD700))
                                    .border(1.dp, Color(0xFF1A0F2E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "★", fontSize = 10.sp, color = Color(0xFF2B1400))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center: Title Pill + Mentioned Username + Level Up Message
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = if (event.isRoomStayLevel) "✨ ROOM LEVEL UP! ✨" else "👑 LEVEL UP! 👑",
                                    color = Color(0xFF241200),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = "@${event.userName}",
                                color = Color(0xFFFFF3B0),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (isCurrentUser) {
                                    if (event.isRoomStayLevel) "You have been leveled up to Room Lv.${event.newLevel}!"
                                    else "You have been leveled up to Lv.${event.newLevel}!"
                                } else {
                                    if (event.isRoomStayLevel) "Leveled up to Room Lv.${event.newLevel} in the room!"
                                    else "Leveled up to Lv.${event.newLevel} in the room!"
                                },
                                color = Color.White.copy(alpha = 0.94f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (event.isRoomStayLevel) {
                                    "${tierInfo.tierName} • +${event.xpBonus} Room XP"
                                } else {
                                    "${tierInfo.tierName} Badge Unlocked"
                                },
                                color = Color(0xFF7DD3FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: Crisp HD Transparent Tier Badge Crest + Level Pill
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0x55FFD700),
                                                accentSecondary.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = tierInfo.badgeDrawableRes),
                                    contentDescription = tierInfo.tierName,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(accentPrimary, accentSecondary)
                                        )
                                    )
                                    .border(0.8.dp, Color(0x99FFF8DC), RoundedCornerShape(50))
                                    .padding(horizontal = 7.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "Lv.${event.newLevel}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Distinct, luxury System Announcement row inside the room's live comment box:
 * Displays right after the 2.8s celebration popup closes:
 *   [HD Crest] [SYSTEM] [Lv.X] <UserName> has been leveled up to Room Lv.X in the room! 🎉
 */
@Composable
fun RoomLevelUpSystemCommentCard(
    comment: LiveRoomComment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = remember(comment.id, comment.level, comment.roomLevelValue) {
        RoomLevelUpEngine.toEvent(comment)
    }
    val tierInfo = remember(event.newLevel) { LevelBadgeSystem.getLevelTierInfo(event.newLevel) }
    val levelTag = if (event.isRoomStayLevel) "Room Lv.${event.newLevel}" else "Lv.${event.newLevel}"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC1E1233),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xCCFFD700),
                    Color(0x99FF9800),
                    Color(0x8038BDF8)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Image(
                painter = painterResource(id = tierInfo.badgeDrawableRes),
                contentDescription = tierInfo.tierName,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFD700))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SYSTEM",
                            color = Color(0xFF231100),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(17.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (tierInfo.gradientColors.size >= 2) tierInfo.gradientColors
                                    else listOf(Color(0xFFFFA000), Color(0xFFFFD700))
                                )
                            )
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = levelTag,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFFFFE57F), fontWeight = FontWeight.ExtraBold)) {
                            append(event.userName)
                        }
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Medium)) {
                            append(" has been leveled up to ")
                        }
                        withStyle(SpanStyle(color = Color(0xFF7DD3FC), fontWeight = FontWeight.ExtraBold)) {
                            append(levelTag)
                        }
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Medium)) {
                            append(" in the room! 🎉")
                        }
                    },
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
