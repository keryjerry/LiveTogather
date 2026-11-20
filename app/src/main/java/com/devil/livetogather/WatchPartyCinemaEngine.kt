package com.devil.livetogather

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CuratedWatchPartyVideo(
    val videoId: String,
    val title: String,
    val category: String
)

object WatchPartyCinemaEngine {
    const val CINEMA_THEME_ID = "cinema_royal_blue_hd"
    const val REQUIRED_ROOM_LEVEL = 5

    private val FOUNDER_UNRESTRICTED_EMAILS = setOf(
        "servicesecure822@gmail.com",
        "service.secure822@gmail.com",
        "devilgroupcontact@gmail.com"
    )

    val CURATED_VIDEOS = listOf(
        CuratedWatchPartyVideo("BddP6PYo2gs", "Kesariya • Bollywood Romantic Hit", "Bollywood"),
        CuratedWatchPartyVideo("gvyUuxdRdR4", "Raataan Lambiyan • Party Jukebox", "Romantic"),
        CuratedWatchPartyVideo("JGwWNGJdvx8", "Shape of You • Global Club Mix", "Party"),
        CuratedWatchPartyVideo("jfKfPfyJRdk", "Lofi Hip Hop Radio • Late Night Chill", "Lo-Fi"),
        CuratedWatchPartyVideo("YR12Z8f1Dh8", "Bollywood Non-Stop Dance Mix", "Dance")
    )

    /**
     * Founder / CEO / Admin (`servicesecure822@gmail.com`) has ZERO criteria or level restrictions.
     * Regular users unlock the Royal Blue Cinema Watch Party Room at Room Level 5+.
     */
    fun isUnrestrictedFounderOrCeo(user: UserProfile): Boolean {
        val emailLower = user.email.trim().lowercase(Locale.US)
        return emailLower in FOUNDER_UNRESTRICTED_EMAILS ||
            user.isFounder() ||
            user.isCeo() ||
            user.isAdmin
    }

    fun canUnlockCinemaWatchParty(user: UserProfile, currentRoomLevel: Int): Boolean {
        if (isUnrestrictedFounderOrCeo(user)) return true
        return currentRoomLevel >= REQUIRED_ROOM_LEVEL
    }

    fun isCinemaWatchPartyActive(room: RoomItem): Boolean {
        return !room.isVideoRoom && (
            room.isWatchPartyActive ||
                room.banner_image_url == CINEMA_THEME_ID ||
                room.eventThemeStyle == CINEMA_THEME_ID
            )
    }

    fun extractYouTubeVideoId(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return ""
        val patterns = listOf(
            Regex("(?:v=|/v/|youtu\\.be/|/embed/|/shorts/)([A-Za-z0-9_-]{11})"),
            Regex("^([A-Za-z0-9_-]{11})$")
        )
        for (p in patterns) {
            val match = p.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }
        return ""
    }

    fun isWelcomeWaveComment(comment: LiveRoomComment): Boolean {
        return comment.systemCommentType == "welcome_wave" ||
            (comment.message.startsWith("👋") && comment.message.contains("welcomed", ignoreCase = true))
    }

    fun isMicInviteComment(comment: LiveRoomComment): Boolean {
        return comment.systemCommentType == "mic_invite"
    }

    /**
     * Sends a real-time Firestore Mic Invitation from Host/Admin to a viewer in the room.
     * The viewer receives an instant live Accept/Decline popup and equips the mic seat upon acceptance.
     */
    fun sendMicInvitation(
        db: FirebaseFirestore,
        roomId: String,
        inviter: UserProfile,
        targetUserId: String,
        targetUserName: String,
        targetSeatIndex: Int
    ) {
        if (roomId.isBlank() || inviter.uid.isBlank() || targetUserId.isBlank()) return
        val inviteId = "micinv_${inviter.uid}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}"
        val inviterName = inviter.name.ifBlank { "Host" }
        val displaySeatNum = targetSeatIndex + 1

        val inviteComment = LiveRoomComment(
            id = inviteId,
            roomId = roomId,
            userId = inviter.uid,
            userName = inviterName,
            userPhoto = inviter.photo,
            tier = inviter.plan.ifBlank { "Standard" },
            message = "🎙️ $inviterName invited $targetUserName to take Mic #$displaySeatNum!",
            timestamp = System.currentTimeMillis(),
            vipLevel = inviter.getVipLevel(),
            isFounder = inviter.isFounder(),
            level = inviter.getEffectiveWealthLevel(),
            recipientName = targetUserName,
            recipientUid = targetUserId,
            systemCommentType = "mic_invite",
            roomLevelValue = targetSeatIndex
        )

        db.collection("rooms").document(roomId)
            .collection("live_comments").document(inviteId)
            .set(inviteComment)
            .addOnFailureListener { e ->
                Log.w("WatchPartyCinema", "Failed to send mic invitation comment: ${e.message}")
            }

        // Also send a real-time notification doc to the invited user
        val notifDoc = mapOf(
            "id" to inviteId,
            "targetUserId" to targetUserId,
            "senderId" to inviter.uid,
            "senderName" to inviterName,
            "senderPhoto" to inviter.photo,
            "type" to "mic_invite",
            "title" to "🎙️ Mic Invitation",
            "message" to "$inviterName invited you to Mic #$displaySeatNum in the live room!",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )
        db.collection("users").document(targetUserId)
            .collection("notifications").document(inviteId)
            .set(notifDoc)
    }

    /**
     * Sends a real-time Firestore Welcome Wave message from the room when any user taps 👋 on an entry card.
     */
    fun sendWelcomeWave(
        db: FirebaseFirestore,
        roomId: String,
        sender: UserProfile,
        targetUserName: String,
        targetUserId: String
    ) {
        if (roomId.isBlank() || sender.uid.isBlank() || targetUserName.isBlank()) return
        val commentId = "wave_${sender.uid}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}"
        val senderName = sender.name.ifBlank { "Member" }
        val comment = LiveRoomComment(
            id = commentId,
            roomId = roomId,
            userId = sender.uid,
            userName = senderName,
            userPhoto = sender.photo,
            tier = sender.plan.ifBlank { "Standard" },
            message = "👋 $senderName welcomed $targetUserName to the room! ✨",
            timestamp = System.currentTimeMillis(),
            vipLevel = sender.getVipLevel(),
            isFounder = sender.isFounder(),
            level = sender.getEffectiveWealthLevel(),
            recipientName = targetUserName,
            recipientUid = targetUserId,
            systemCommentType = "welcome_wave"
        )
        db.collection("rooms").document(roomId)
            .collection("live_comments").document(commentId)
            .set(comment)
            .addOnFailureListener { e ->
                Log.w("WatchPartyCinema", "Failed to send welcome wave: ${e.message}")
            }
    }

    fun updateRoomWatchPartyState(
        db: FirebaseFirestore,
        room: RoomItem,
        isActive: Boolean,
        videoId: String = room.watchPartyVideoId.ifBlank { CURATED_VIDEOS.first().videoId },
        videoTitle: String = room.watchPartyVideoTitle.ifBlank { CURATED_VIDEOS.first().title },
        isPlaying: Boolean = room.watchPartyIsPlaying,
        updatedBy: String
    ) {
        if (room.id.isBlank()) return
        val updates = mutableMapOf<String, Any>(
            "isWatchPartyActive" to isActive,
            "watchPartyVideoId" to videoId,
            "watchPartyVideoTitle" to videoTitle,
            "watchPartyIsPlaying" to isPlaying,
            "watchPartyUpdatedBy" to updatedBy,
            "watchPartyUpdatedAt" to System.currentTimeMillis()
        )
        if (isActive) {
            updates["banner_image_url"] = CINEMA_THEME_ID
            updates["eventThemeStyle"] = CINEMA_THEME_ID
        }
        db.collection("rooms").document(room.id).update(updates)
        if (room.category.isNotBlank()) {
            db.collection("intent_categories").document(room.category)
                .collection("rooms").document(room.id)
                .update(updates)
        }
    }

    fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    fun claimRoomTaskReward(
        context: Context,
        db: FirebaseFirestore,
        userId: String,
        roomId: String,
        taskKey: String,
        diamondBonus: Long,
        roomXpBonus: Long,
        onClaimed: () -> Unit
    ) {
        if (userId.isBlank() || roomId.isBlank()) return
        val fullKey = "${todayKey()}_${taskKey}"
        val safeRoomKey = RoomLevelUpEngine.sanitizeRoomKey(roomId)
        val userRef = db.collection("users").document(userId)

        userRef.update(
            mapOf(
                "diamonds" to FieldValue.increment(diamondBonus),
                "wealthXp" to FieldValue.increment(roomXpBonus / 2L),
                "roomXpMap.$safeRoomKey" to FieldValue.increment(roomXpBonus),
                "claimedRoomTasks" to FieldValue.arrayUnion(fullKey)
            )
        ).addOnSuccessListener {
            Toast.makeText(context, "🎁 Claimed +$diamondBonus 💎 & +$roomXpBonus Room XP!", Toast.LENGTH_SHORT).show()
            onClaimed()
        }.addOnFailureListener {
            Toast.makeText(context, "Could not claim reward right now. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Boxed Synced YouTube Watch Party Screen + Floating HD 3D Treasure Chest
 * Preserves all existing top/bottom room controls.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CinemaWatchPartyVideoBox(
    room: RoomItem,
    canControlVideo: Boolean,
    currentUserName: String,
    currentRoomStaySeconds: Long,
    onTogglePlay: () -> Unit,
    onOpenVideoPicker: () -> Unit,
    onClaimTreasureChest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoId = room.watchPartyVideoId.ifBlank { WatchPartyCinemaEngine.CURATED_VIDEOS.first().videoId }
    val videoTitle = room.watchPartyVideoTitle.ifBlank { WatchPartyCinemaEngine.CURATED_VIDEOS.first().title }
    val isPlaying = room.watchPartyIsPlaying

    // Real-time 3-minute (180s) Treasure Chest cycle countdown
    var chestCooldownSec by remember(room.id) { mutableLongStateOf(180L) }
    LaunchedEffect(room.id) {
        while (true) {
            delay(1000L)
            if (chestCooldownSec > 0L) chestCooldownSec -= 1L
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chest_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chest_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(176.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF061130),
            border = BorderStroke(
                width = 1.4.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF60A5FA), Color(0xFFFFD700), Color(0xFF38BDF8))
                )
            ),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Real YouTube Synced Embed WebView
                val embedUrl = remember(videoId, isPlaying) {
                    val auto = if (isPlaying) 1 else 0
                    "https://www.youtube.com/embed/$videoId?autoplay=$auto&playsinline=1&rel=0&modestbranding=1&controls=1"
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#061130"))
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadUrl(embedUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.tag != embedUrl) {
                            webView.tag = embedUrl
                            webView.loadUrl(embedUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top-Right Title & Host Watch Party Controls Strip
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        color = Color(0xCC061336),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0x8838BDF8))
                    ) {
                        Text(
                            text = "▶ $videoTitle",
                            color = Color(0xFFFFE57F),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 148.dp)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (canControlVideo) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xD91D4ED8))
                                .border(1.dp, Color(0xAA93C5FD), CircleShape)
                                .clickable { onTogglePlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause Synced Video",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xD91E3A8A))
                                .border(1.dp, Color(0xAAFFD700), CircleShape)
                                .clickable { onOpenVideoPicker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Pick YouTube Video",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Synced Pause Status Overlay (matches reference UX cleanly)
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x88050D26))
                            .clickable(enabled = canControlVideo) { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xCC081538),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color(0x99FFD700))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${room.watchPartyUpdatedBy.ifBlank { currentUserName.ifBlank { "Host" } }} paused the video play",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top-Left Floating Photorealistic HD 3D Treasure Chest with Countdown
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 4.dp)
                .clickable {
                    if (chestCooldownSec == 0L) {
                        chestCooldownSec = 180L
                        onClaimTreasureChest()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = if (chestCooldownSec == 0L) 0.55f * glowAlpha else 0.22f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_room_treasure_chest_hd),
                    contentDescription = "Treasure Chest",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (chestCooldownSec == 0L) Color(0xFFFFD700) else Color(0xCC0A1942)
                    )
                    .border(
                        width = 0.8.dp,
                        color = if (chestCooldownSec == 0L) Color.White else Color(0x88FFD700),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                val mins = chestCooldownSec / 60
                val secs = chestCooldownSec % 60
                Text(
                    text = if (chestCooldownSec == 0L) "CLAIM!" else String.format(Locale.US, "%02d:%02d", mins, secs),
                    color = if (chestCooldownSec == 0L) Color(0xFF1E1000) else Color(0xFFFFE57F),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * 10 Photorealistic HD 3D Royal Blue Velvet Sofa Mic Seats (2 Rows x 5)
 * Mic icon, Lock icon, or Occupied User Avatar sits cleanly INSIDE the center of each plush sofa chair.
 */
@Composable
fun RoyalBlueSofaTenMicStage(
    seats: List<MicSeat>,
    currentUserId: String,
    isLocalTalking: Boolean,
    allProfiles: List<UserProfile>,
    activeEmojiReactions: Map<String, String>,
    onSeatClick: (MicSeat) -> Unit,
    modifier: Modifier = Modifier
) {
    val tenSeats = remember(seats) {
        (0 until 10).map { idx ->
            seats.find { it.seatIndex == idx } ?: MicSeat(
                seatIndex = idx,
                label = "${idx + 1}",
                uid = "",
                name = "",
                photo = "",
                role = if (idx == 0) "host" else "speaker"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(tenSeats.subList(0, 5), tenSeats.subList(5, 10)).forEach { rowSeats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                rowSeats.forEach { seat ->
                    val isOccupied = seat.uid.isNotBlank()
                    val isTalkingNow = isOccupied && (seat.isTalking || (seat.uid == currentUserId && isLocalTalking)) && !seat.isMuted
                    val occupantProfile = remember(seat.uid, allProfiles) {
                        if (seat.uid.isNotBlank()) allProfiles.find { it.uid == seat.uid } else null
                    }
                    val reactionEmoji = if (seat.uid.isNotBlank()) activeEmojiReactions[seat.uid] else null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSeatClick(seat) }
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Real Photorealistic HD 3D Royal Blue Velvet Sofa Armchair
                            Image(
                                painter = painterResource(id = R.drawable.ic_cinema_sofa_royal_blue),
                                contentDescription = "Royal Blue Sofa Seat ${seat.seatIndex + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Mic / Lock / User Avatar centered RIGHT INSIDE the Sofa Chair
                            if (isOccupied) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isTalkingNow) 2.2.dp else 1.4.dp,
                                            color = if (isTalkingNow) Color(0xFF4ADE80) else Color(0xFFFFD700),
                                            shape = CircleShape
                                        )
                                        .background(Color(0xFF0A1B47)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val photoUrl = seat.photo.ifBlank { occupantProfile?.photo ?: "" }
                                    if (photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = seat.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = seat.name.ifBlank { "U" }.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                // Mic Mute / Active micro-dot on bottom-right of sofa avatar
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 9.dp, bottom = 10.dp)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (seat.isMuted) Color(0xFFEF4444) else Color(0xFF10B981))
                                        .border(1.dp, Color(0xFF07143A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            } else {
                                // Empty or Locked Sofa -> Crisp translucent center orb with Lock or Mic
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xAA07143A))
                                        .border(0.9.dp, Color(0x8893C5FD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (seat.isLocked) Icons.Default.Lock else Icons.Default.Mic,
                                        contentDescription = if (seat.isLocked) "Locked" else "Take Mic",
                                        tint = if (seat.isLocked) Color(0xFFFFD700) else Color(0xFFE0F2FE),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            if (!reactionEmoji.isNullOrBlank()) {
                                Text(
                                    text = reactionEmoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }

                        // Seat number + User name pill right under sofa
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xA6050E2A))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isOccupied) Color(0xFF2563EB) else Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${seat.seatIndex + 1}",
                                    color = Color.White,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isOccupied) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = seat.name.ifBlank { "Guest" },
                                    color = Color(0xFFFFF3C4),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 48.dp)
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
 * Sleek Royal Blue & Gold Room Task Reward Banner right above the live comments box.
 */
@Composable
fun RoomTaskRewardBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xAAFFD700)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xD91E3A8A),
                            Color(0xD91D4ED8),
                            Color(0xCC0F295E)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "📋", fontSize = 16.sp)
                Text(
                    text = "Complete the room task to get more rewards!",
                    color = Color(0xFFFFF8DC),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Room Tasks",
                    tint = Color(0xFF0A1638),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Real-Time Firestore Room Tasks Dialog (Stay 5m / Welcome Wave / Gift)
 */
@Composable
fun RoomTaskRewardsDialog(
    roomId: String,
    currentUser: UserProfile,
    currentRoomStaySeconds: Long,
    hasWavedInRoom: Boolean,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var claimedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    val today = remember { WatchPartyCinemaEngine.todayKey() }

    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { snap ->
                @Suppress("UNCHECKED_CAST")
                val list = (snap.get("claimedRoomTasks") as? List<String>) ?: emptyList()
                claimedKeys = list.toSet()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B1A42),
            border = BorderStroke(1.4.dp, Color(0xAAFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 Daily Room Tasks",
                        color = Color(0xFFFFE57F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                val stayReady = currentRoomStaySeconds >= 300L
                val stayClaimed = "${today}_stay_5m" in claimedKeys
                RoomTaskRowItem(
                    title = "Stay 5 Minutes in Room",
                    subtitle = "+2 💎 & +50 Room XP (${(currentRoomStaySeconds / 60)}/5 min)",
                    isReady = stayReady,
                    isClaimed = stayClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "stay_5m", 2L, 50L
                        ) { claimedKeys = claimedKeys + "${today}_stay_5m" }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                val waveClaimed = "${today}_welcome_wave" in claimedKeys
                RoomTaskRowItem(
                    title = "Send a Welcome Wave (👋)",
                    subtitle = "+1 💎 & +25 Room XP (Tap 👋 on any room entry)",
                    isReady = hasWavedInRoom,
                    isClaimed = waveClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "welcome_wave", 1L, 25L
                        ) { claimedKeys = claimedKeys + "${today}_welcome_wave" }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                val dailyCheckInClaimed = "${today}_room_checkin" in claimedKeys
                RoomTaskRowItem(
                    title = "Daily Royal Room Check-In",
                    subtitle = "+1 💎 & +30 Room XP",
                    isReady = true,
                    isClaimed = dailyCheckInClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "room_checkin", 1L, 30L
                        ) { claimedKeys = claimedKeys + "${today}_room_checkin" }
                    }
                )
            }
        }
    }
}

@Composable
private fun RoomTaskRowItem(
    title: String,
    subtitle: String,
    isReady: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF13275C),
        border = BorderStroke(0.8.dp, Color(0x4493C5FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF93C5FD), fontSize = 10.5.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClaim,
                enabled = isReady && !isClaimed,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1E1000),
                    disabledContainerColor = Color(0xFF1E3A8A),
                    disabledContentColor = Color(0xAAFFFFFF)
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = when {
                        isClaimed -> "Claimed ✓"
                        isReady -> "Claim"
                        else -> "In Progress"
                    },
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Host/Admin YouTube Video Picker & Direct URL Sync Dialog
 */
@Composable
fun YouTubeVideoPickerDialog(
    currentVideoTitle: String,
    onSelectVideo: (videoId: String, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var customTitle by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0A173B),
            border = BorderStroke(1.4.dp, Color(0xAA60A5FA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎬 Synced YouTube Watch Party",
                        color = Color(0xFFFFE57F),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Text(
                    text = "Now Playing: $currentVideoTitle",
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("Paste any YouTube URL or Video ID...", fontSize = 12.sp, color = Color(0x88FFFFFF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0x6693C5FD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    placeholder = { Text("Optional video title", fontSize = 12.sp, color = Color(0x88FFFFFF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0x6693C5FD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val extracted = WatchPartyCinemaEngine.extractYouTubeVideoId(urlInput)
                        if (extracted.isBlank()) {
                            Toast.makeText(context, "Please paste a valid YouTube link or 11-character Video ID", Toast.LENGTH_SHORT).show()
                        } else {
                            onSelectVideo(extracted, customTitle.ifBlank { "YouTube Watch Party" })
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("▶ Play & Sync for Entire Room", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Popular Ready-to-Play Videos:", color = Color(0xFFFFE57F), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.height(170.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(WatchPartyCinemaEngine.CURATED_VIDEOS) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF132860),
                            border = BorderStroke(0.8.dp, Color(0x5560A5FA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectVideo(item.videoId, item.title)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.category, color = Color(0xFF7DD3FC), fontSize = 9.5.sp)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2-Step Entry Action Pill attached beside Audio Room entrance cards:
 * Step 1: Shows 👋 Wave -> taps to send real-time Welcome message to the newcomer.
 * Step 2 (for Host/Admin/Founder/Staff): Transforms in the exact same slot into a small 🎙️ Mic Invite icon
 *         -> taps to send a real-time Mic Seat Invitation popup to the viewer!
 */
@Composable
fun WelcomeWaveActionButton(
    hasWaved: Boolean,
    hasInvitedMic: Boolean,
    canInviteToMic: Boolean,
    onWaveClick: () -> Unit,
    onInviteMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showMicInvite = hasWaved && canInviteToMic && !hasInvitedMic
    val isCompleted = (hasWaved && !canInviteToMic) || hasInvitedMic

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(
            width = 0.9.dp,
            color = when {
                isCompleted -> Color(0x884ADE80)
                showMicInvite -> Color(0xCCFFD700)
                else -> Color(0xCC93C5FD)
            }
        ),
        modifier = modifier.clickable(enabled = !isCompleted) {
            if (!hasWaved) onWaveClick() else if (showMicInvite) onInviteMicClick()
        }
    ) {
        Row(
            modifier = Modifier
                .background(
                    when {
                        isCompleted -> Brush.horizontalGradient(listOf(Color(0xCC14532D), Color(0xCC166534)))
                        showMicInvite -> Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF0EA5E9)))
                        else -> Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB)))
                    }
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (showMicInvite) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Invite to Mic",
                    tint = Color(0xFFFFE57F),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Mic",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            } else {
                Text(
                    text = when {
                        hasInvitedMic -> "✓ Invited"
                        hasWaved -> "✓ Hi"
                        else -> "👋"
                    },
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Real-Time Incoming Mic Invitation Popup for the invited viewer.
 * Tapping "Accept & Take Mic" immediately equips the viewer onto the invited mic seat!
 */
@Composable
fun MicInviteReceivedDialog(
    inviterName: String,
    inviterPhoto: String,
    seatIndex: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = onDecline) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A173B),
            border = BorderStroke(1.5.dp, Color(0xCCFFD700)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFFFFD700)))
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (inviterPhoto.isNotBlank()) {
                        AsyncImage(
                            model = inviterPhoto,
                            contentDescription = inviterName,
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
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎙️ Mic Invitation!",
                    color = Color(0xFFFFE57F),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$inviterName invited you to join Mic #${seatIndex + 1} on stage!",
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Decline", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.weight(1.25f)
                    ) {
                        Text("Accept & Take Mic", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Subtle Royal Blue & Gold comment strip showing when Host/Admin invites a viewer to a mic seat.
 */
@Composable
fun MicInviteCommentCard(
    comment: LiveRoomComment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC11265C),
        border = BorderStroke(0.9.dp, Color(0x8838BDF8)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFFE57F), fontWeight = FontWeight.Bold)) {
                        append(comment.userName.ifBlank { "Host" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" invited ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF7DD3FC), fontWeight = FontWeight.Bold)) {
                        append(comment.recipientName.ifBlank { "Guest" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" to Mic #${comment.roomLevelValue + 1}! 🎙️")
                    }
                },
                fontSize = 11.5.sp,
                lineHeight = 14.5.sp
            )
        }
    }
}

/**
 * Crisp Royal Blue & Gold Welcome Wave comment card when someone welcomes a newcomer to the room.
 */
@Composable
fun WelcomeWaveCommentCard(
    comment: LiveRoomComment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC0D2258),
        border = BorderStroke(0.9.dp, Color(0x88FFD700)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "👋", fontSize = 14.sp)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFFE57F), fontWeight = FontWeight.Bold)) {
                        append(comment.userName.ifBlank { "Member" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" welcomed ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF7DD3FC), fontWeight = FontWeight.Bold)) {
                        append(comment.recipientName.ifBlank { "New Guest" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" to the room! ✨")
                    }
                },
                fontSize = 11.5.sp,
                lineHeight = 14.5.sp
            )
        }
    }
}
