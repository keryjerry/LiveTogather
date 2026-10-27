package com.devil.livetogather

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// VIP CHAT FRAME DATA MODEL & CATALOG
// -------------------------------------------------------------
data class VipChatFrameItem(
    val id: String,
    val name: String,
    val vipLevel: Int,
    val isFounder: Boolean = false,
    val borderBrush: Brush,
    val backgroundBrush: Brush,
    val textColor: Color = Color.White,
    val accentColor: Color,
    val secondaryAccentColor: Color = Color(0xFFFFD700),
    val gemColor: Color = Color(0xFFFFD700),
    val cornerWingColor: Color = Color(0xFFFFD700),
    val badgeLabel: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color = Color.White,
    val cornerGemEmoji: String = "✨",
    val description: String = "Exclusive VIP Chat Message Frame"
)

val VIP_CHAT_FRAMES_CATALOG: List<VipChatFrameItem> = listOf(
    VipChatFrameItem(
        id = "chat_frame_silver_knight",
        name = "Silver Knight (VIP 1)",
        vipLevel = 1,
        borderBrush = Brush.linearGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF00E5FF), Color(0xFFE2E8F0))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF131D2E), Color(0xFF0D1420))
        ),
        accentColor = Color(0xFF00E5FF),
        secondaryAccentColor = Color(0xFFE2E8F0),
        gemColor = Color(0xFF00E5FF),
        cornerWingColor = Color(0xFF94A3B8),
        badgeLabel = "VIP 1",
        badgeBgColor = Color(0xFF0284C7),
        cornerGemEmoji = "💎",
        description = "Beveled silver crystal bubble for Knight VIP 1"
    ),
    VipChatFrameItem(
        id = "chat_frame_golden_paladin",
        name = "Golden Paladin (VIP 2)",
        vipLevel = 2,
        borderBrush = Brush.linearGradient(
            listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFFFE082))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF2E1C07), Color(0xFF1A0F03))
        ),
        accentColor = Color(0xFFFFD700),
        secondaryAccentColor = Color(0xFFFF8F00),
        gemColor = Color(0xFFFFEA00),
        cornerWingColor = Color(0xFFFFD700),
        badgeLabel = "VIP 2",
        badgeBgColor = Color(0xFFB45309),
        cornerGemEmoji = "⭐",
        description = "Regal golden baroque frame with imperial amber glow"
    ),
    VipChatFrameItem(
        id = "chat_frame_imperial_amethyst",
        name = "Imperial Amethyst (VIP 3)",
        vipLevel = 3,
        borderBrush = Brush.linearGradient(
            listOf(Color(0xFFE879F9), Color(0xFFA855F7), Color(0xFFFFD700), Color(0xFFE879F9))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF2C103A), Color(0xFF160820))
        ),
        accentColor = Color(0xFFE879F9),
        secondaryAccentColor = Color(0xFFFFD700),
        gemColor = Color(0xFFA855F7),
        cornerWingColor = Color(0xFFE879F9),
        badgeLabel = "VIP 3",
        badgeBgColor = Color(0xFF7E22CE),
        cornerGemEmoji = "🔮",
        description = "Royal amethyst crystal with gold corner embellishments"
    ),
    VipChatFrameItem(
        id = "chat_frame_celestial_dragon",
        name = "Celestial Dragon (VIP 4+)",
        vipLevel = 4,
        borderBrush = Brush.sweepGradient(
            listOf(Color(0xFF00F5FF), Color(0xFFFF1744), Color(0xFFFFD700), Color(0xFF00F5FF))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF102636), Color(0xFF1F0D2E))
        ),
        accentColor = Color(0xFF00F5FF),
        secondaryAccentColor = Color(0xFFFFD700),
        gemColor = Color(0xFF00F5FF),
        cornerWingColor = Color(0xFFFFD700),
        badgeLabel = "VIP 👑",
        badgeBgColor = Color(0xFFDC2626),
        cornerGemEmoji = "🐉",
        description = "Cosmic dragon sweep bubble with pulsating auroras"
    ),
    VipChatFrameItem(
        id = "chat_frame_mystic_scorpio",
        name = "Mystic Scorpio (Monthly Card)",
        vipLevel = 1,
        borderBrush = Brush.linearGradient(
            listOf(Color(0xFF00E5FF), Color(0xFF4F46E5), Color(0xFFFFD700), Color(0xFF00E5FF))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF0A1D2E), Color(0xFF050E17))
        ),
        accentColor = Color(0xFF00E5FF),
        secondaryAccentColor = Color(0xFFFFD700),
        gemColor = Color(0xFF00E5FF),
        cornerWingColor = Color(0xFF00E5FF),
        badgeLabel = "SCORPIO 🦂",
        badgeBgColor = Color(0xFF0284C7),
        cornerGemEmoji = "✨",
        description = "Seasonal Monthly Card exclusive cyan & gold baroque frame"
    ),
    VipChatFrameItem(
        id = "chat_frame_founder_supreme",
        name = "LiveTogather Founder Supreme",
        vipLevel = 10,
        isFounder = true,
        borderBrush = Brush.sweepGradient(
            listOf(Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFFA855F7), Color(0xFFFFEA00), Color(0xFFFFD700))
        ),
        backgroundBrush = Brush.linearGradient(
            listOf(Color(0xFF2E0938), Color(0xFF1B0522), Color(0xFF330C16))
        ),
        textColor = Color(0xFFFFFDF5),
        accentColor = Color(0xFFFFD700),
        secondaryAccentColor = Color(0xFFFF1744),
        gemColor = Color(0xFFFFEA00),
        cornerWingColor = Color(0xFFFFD700),
        badgeLabel = "FOUNDER 👑",
        badgeBgColor = Color(0xFF8B1838),
        cornerGemEmoji = "⚡",
        description = "Supreme Founder & CEO Exclusive Masterpiece with baroque gold wings & glowing ruby gems"
    )
)

fun getVipChatFrame(frameId: String?, vipLevel: Int = 0, isFounder: Boolean = false): VipChatFrameItem? {
    if (isFounder) return VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_founder_supreme" }
    if (!frameId.isNullOrBlank()) {
        val found = VIP_CHAT_FRAMES_CATALOG.find { it.id == frameId }
        if (found != null) return found
    }
    return when {
        vipLevel >= 4 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_celestial_dragon" }
        vipLevel == 3 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_imperial_amethyst" }
        vipLevel == 2 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_golden_paladin" }
        vipLevel == 1 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_silver_knight" }
        else -> null
    }
}

// -------------------------------------------------------------
// ORNATE LUXURY DECORATIVE EMBELLISHMENTS (Reference UI Match)
// -------------------------------------------------------------
@Composable
fun OrnateCornerFlourish(
    isTop: Boolean,
    isLeft: Boolean,
    primaryColor: Color,
    accentColor: Color,
    gemColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            if (isTop && isLeft) {
                moveTo(0f, h)
                quadraticBezierTo(0f, 0f, w, 0f)
                lineTo(w * 0.7f, h * 0.25f)
                quadraticBezierTo(w * 0.25f, h * 0.25f, w * 0.25f, h * 0.7f)
                close()
            } else if (isTop && !isLeft) {
                moveTo(w, h)
                quadraticBezierTo(w, 0f, 0f, 0f)
                lineTo(w * 0.3f, h * 0.25f)
                quadraticBezierTo(w * 0.75f, h * 0.25f, w * 0.75f, h * 0.7f)
                close()
            } else if (!isTop && isLeft) {
                moveTo(0f, 0f)
                quadraticBezierTo(0f, h, w, h)
                lineTo(w * 0.7f, h * 0.75f)
                quadraticBezierTo(w * 0.25f, h * 0.75f, w * 0.25f, h * 0.3f)
                close()
            } else {
                moveTo(w, 0f)
                quadraticBezierTo(w, h, 0f, h)
                lineTo(w * 0.3f, h * 0.75f)
                quadraticBezierTo(w * 0.75f, h * 0.75f, w * 0.75f, h * 0.3f)
                close()
            }
        }
        drawPath(path = path, brush = Brush.linearGradient(listOf(primaryColor, accentColor)))

        val gemCenter = Offset(
            x = if (isLeft) w * 0.38f else w * 0.62f,
            y = if (isTop) h * 0.38f else h * 0.62f
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, gemColor, accentColor),
                center = gemCenter,
                radius = w * 0.2f
            ),
            radius = w * 0.2f,
            center = gemCenter
        )
    }
}

@Composable
fun OrnateTopCrest(
    primaryColor: Color,
    accentColor: Color,
    gemColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 36.dp, height = 10.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val wingPath = Path().apply {
            moveTo(cx, cy)
            quadraticBezierTo(cx - w * 0.25f, 0f, 0f, cy * 0.6f)
            quadraticBezierTo(cx - w * 0.2f, cy * 1.2f, cx, cy)
            quadraticBezierTo(cx + w * 0.25f, 0f, w, cy * 0.6f)
            quadraticBezierTo(cx + w * 0.2f, cy * 1.2f, cx, cy)
            close()
        }
        drawPath(wingPath, brush = Brush.horizontalGradient(listOf(primaryColor, accentColor, primaryColor)))

        val gemPath = Path().apply {
            moveTo(cx, 0f)
            lineTo(cx + 4.dp.toPx(), cy)
            lineTo(cx, h)
            lineTo(cx - 4.dp.toPx(), cy)
            close()
        }
        drawPath(gemPath, brush = Brush.radialGradient(listOf(Color.White, gemColor, accentColor), center = Offset(cx, cy), radius = 6.dp.toPx()))
    }
}

@Composable
fun OrnateBottomSeal(
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 24.dp, height = 6.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        val path = Path().apply {
            moveTo(cx - w * 0.45f, 0f)
            quadraticBezierTo(cx, h, cx + w * 0.45f, 0f)
            quadraticBezierTo(cx, h * 0.4f, cx - w * 0.45f, 0f)
            close()
        }
        drawPath(path, brush = Brush.horizontalGradient(listOf(primaryColor, accentColor, primaryColor)))
    }
}

@Composable
fun EntranceBannerTopCrest(
    emblemText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 64.dp, height = 18.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            val path = Path().apply {
                moveTo(cx, h)
                quadraticBezierTo(cx - w * 0.25f, 0f, 0f, h * 0.5f)
                quadraticBezierTo(cx - w * 0.2f, h * 1.2f, cx, h)
                quadraticBezierTo(cx + w * 0.2f, h * 1.2f, w, h * 0.5f)
                quadraticBezierTo(w, 0f, cx, h)
                close()
            }
            drawPath(path, brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFEA00), Color(0xFFFF8F00), Color(0xFFFFD700))))
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFFFEA00), Color(0xFFFF8F00), Color(0xFFB45309))))
                .border(1.dp, Color(0xFFFFF9E6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emblemText,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun EntranceBannerBottomCrest(
    emblemText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 54.dp, height = 16.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            val path = Path().apply {
                moveTo(cx, 0f)
                cubicTo(cx - w * 0.25f, h, 0f, h, 0f, h * 0.5f)
                quadraticBezierTo(cx - w * 0.2f, 0f, cx, 0f)
                quadraticBezierTo(cx + w * 0.2f, 0f, w, h * 0.5f)
                quadraticBezierTo(w, h, cx, 0f)
                close()
            }
            drawPath(path, brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFEA00), Color(0xFFFF8F00), Color(0xFFFFD700))))
        }

        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFFFEA00), Color(0xFFFF8F00), Color(0xFFB45309))))
                .border(1.dp, Color(0xFFFFF9E6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emblemText,
                color = Color.White,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// -------------------------------------------------------------
// VIP CHAT BUBBLE COMPOSABLE (With Ornate Luxury Corners & Styling)
// -------------------------------------------------------------
@Composable
fun VipStyledChatBubble(
    messageText: String,
    isMe: Boolean,
    chatFrameId: String? = null,
    vipLevel: Int = 0,
    isFounder: Boolean = false,
    timestampText: String = "",
    modifier: Modifier = Modifier
) {
    val frameItem = getVipChatFrame(chatFrameId, vipLevel, isFounder)

    if (frameItem != null) {
        val infiniteTransition = rememberInfiniteTransition(label = "VipBubbleGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowAlpha"
        )

        Box(
            modifier = modifier
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .shadow(
                    elevation = if (frameItem.isFounder) 10.dp else 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = frameItem.accentColor,
                    spotColor = frameItem.accentColor
                )
        ) {
            // Main Framed Container with Dual Metallic Border & Rich Gradient
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(frameItem.backgroundBrush)
                    .border(
                        BorderStroke(
                            width = if (frameItem.isFounder) 2.2.dp else 1.8.dp,
                            brush = frameItem.borderBrush
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(1.dp)
                    .border(
                        BorderStroke(
                            width = 0.75.dp,
                            color = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(13.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = messageText,
                        color = frameItem.textColor,
                        fontSize = 13.5.sp,
                        fontWeight = if (frameItem.isFounder) FontWeight.SemiBold else FontWeight.Medium,
                        lineHeight = 18.sp
                    )

                    if (timestampText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = timestampText,
                            fontSize = 8.5.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = if (isMe) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Top Center Ornate Winged Crest
            OrnateTopCrest(
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                gemColor = frameItem.gemColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            )

            // Bottom Center Ornate Seal
            OrnateBottomSeal(
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 3.dp)
            )

            // 4 Sculpted Baroque Corner Wings with Gemstones
            OrnateCornerFlourish(
                isTop = true,
                isLeft = true,
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                gemColor = frameItem.gemColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-2).dp, y = (-2).dp)
            )

            OrnateCornerFlourish(
                isTop = true,
                isLeft = false,
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                gemColor = frameItem.gemColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            )

            OrnateCornerFlourish(
                isTop = false,
                isLeft = true,
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                gemColor = frameItem.gemColor,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-2).dp, y = 2.dp)
            )

            OrnateCornerFlourish(
                isTop = false,
                isLeft = false,
                primaryColor = frameItem.secondaryAccentColor,
                accentColor = frameItem.accentColor,
                gemColor = frameItem.gemColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            )
        }
    } else {
        // Standard Chat Bubble (Fallback for non-VIP)
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isMe) BrandCoral else BrandCard)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Column {
                Text(messageText, color = Color.White, fontSize = 13.sp)
                if (timestampText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        timestampText,
                        fontSize = 8.5.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// USER CHAT BADGES ROW (Displays 3D Founder, CEO, Tier, VIP Badges)
// -------------------------------------------------------------
@Composable
fun UserChatBadgesRow(
    userProfile: UserProfile?,
    level: Int,
    charm: Int,
    pkLevel: Int,
    medal: Int,
    isFounder: Boolean = false,
    isCeo: Boolean = false,
    vipLevel: Int = 0,
    modifier: Modifier = Modifier
) {
    val tierInfo = LevelBadgeSystem.getLevelTierInfo(level)
    val isApexFounder = isFounder || userProfile?.isFounder() == true
    val isApexCeo = !isApexFounder && (isCeo || userProfile?.isCeo() == true)
    val officialBadgeRes = userProfile?.getOfficialRoleBadgeRes()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        // 1. 👑 Supreme Founder 3D Winged Golden Badge (Large 3D Official)
        if (isApexFounder) {
            Image(
                painter = painterResource(id = R.drawable.badge_livetogather_founder),
                contentDescription = "LiveTogather Founder 3D Badge",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(26.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(4.dp))
            )
        } else if (isApexCeo) {
            // 2. ⚡ LiveTogather CEO 3D Badge
            Image(
                painter = painterResource(id = R.drawable.badge_livetogather_ceo),
                contentDescription = "LiveTogather CEO 3D Badge",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(24.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(4.dp))
            )
        } else if (officialBadgeRes != null) {
            // 3. 🛡️ Official Appointed Staff 3D Badge (Analyzer, Patrol, Volunteer)
            Image(
                painter = painterResource(id = officialBadgeRes),
                contentDescription = "Official Staff Badge",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(20.dp)
                    .shadow(3.dp, shape = RoundedCornerShape(4.dp))
            )
        }

        // 4. 🏆 Wealth/Level 3D Crest Badge (Emperor, Diamond, Dragon, Royal, Sapphire, Bronze)
        Image(
            painter = painterResource(id = tierInfo.badgeDrawableRes),
            contentDescription = tierInfo.tierName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(20.dp)
        )

        // 5. 💎 VIP Badge Pill (If VIP Level > 0)
        val effVip = if (vipLevel > 0) vipLevel else (userProfile?.getVipLevel() ?: 0)
        if (effVip > 0 || isApexFounder) {
            Surface(
                color = if (isApexFounder) Color(0xFF8B1838) else Color(0xFFB45309),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, BrandGold),
                modifier = Modifier.height(17.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isApexFounder) "👑 VIP 10" else "👑 VIP $effVip",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // 6. ⚔️ Crossed Swords Rank Badge (PK Level)
        SilverSwordsRankBadge(rank = pkLevel)

        // 7. 🌟 Level Badge Pill
        BlueLevelBadgePill(level = level)

        // 8. 💖 Sapphire Charm Gem Pill
        SapphireCharmBadgePill(level = charm)

        // 9. 🏅 Golden Medal Follower Pill
        GoldenMedalBadgePill(level = medal)
    }
}

// -------------------------------------------------------------
// GRAND ORNATE LUXURY ENTRANCE CARD (Matches Image 2 Hacker Banner)
// -------------------------------------------------------------
@Composable
fun OrnateLuxuryEntranceCard(
    userName: String,
    avatarUrl: String,
    userProfile: UserProfile?,
    level: Int,
    charm: Int,
    pkLevel: Int,
    medal: Int,
    isFounder: Boolean = false,
    vipLevel: Int = 0,
    modifier: Modifier = Modifier
) {
    val isApexFounder = isFounder || userProfile?.isFounder() == true
    val isApexCeo = !isApexFounder && userProfile?.isCeo() == true
    val effVip = if (vipLevel > 0) vipLevel else if (isApexFounder) 10 else 1
    val tierInfo = LevelBadgeSystem.getLevelTierInfo(level)

    val infiniteTransition = rememberInfiniteTransition(label = "EntranceCardGlow")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "BorderGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isApexFounder) Color(0xFFFF1744) else Color(0xFFFFD700),
                spotColor = Color(0xFFFFD700)
            )
    ) {
        // Main Container Card with Golden Filigree Metallic Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        if (isApexFounder) listOf(Color(0xFF2E0938), Color(0xFF1B0522), Color(0xFF330C16))
                        else listOf(Color(0xFF3B2207), Color(0xFF241403), Color(0xFF1A0E02))
                    )
                )
                .border(
                    BorderStroke(
                        width = 2.2.dp,
                        brush = Brush.linearGradient(
                            if (isApexFounder) listOf(Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFFFFEA00), Color(0xFFFFD700))
                            else listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFFD700), Color(0xFFFF8F00))
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. User Avatar with Ornate Frame
                FlowingAvatarFrame(
                    photoUrl = avatarUrl,
                    frameId = if (isApexFounder) "frame_livetogather_founder" else (userProfile?.activeFrame ?: "frame_imperial_gold"),
                    avatarSize = 36.dp,
                    frameSize = 54.dp,
                    showMicEffect = false
                )

                Spacer(modifier = Modifier.width(10.dp))

                // 2. Identity Column
                Column(modifier = Modifier.weight(1f)) {
                    // Line 1: Username with sparkles & crown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "★ ${userName.uppercase()} ★",
                            color = if (isApexFounder) Color(0xFFFFEA00) else Color(0xFFFFD700),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isApexFounder) {
                            Text(text = "👑", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Line 2: 3D Badges Row (Founder, CEO, Tier, VIP)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isApexFounder) {
                            Image(
                                painter = painterResource(id = R.drawable.badge_livetogather_founder),
                                contentDescription = "Founder Badge",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        if (isApexCeo || isApexFounder) {
                            Image(
                                painter = painterResource(id = R.drawable.badge_livetogather_ceo),
                                contentDescription = "CEO Badge",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        Image(
                            painter = painterResource(id = tierInfo.badgeDrawableRes),
                            contentDescription = tierInfo.tierName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(22.dp)
                        )
                        Surface(
                            color = if (isApexFounder) Color(0xFF8B1838) else Color(0xFFB45309),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, BrandGold),
                            modifier = Modifier.height(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isApexFounder) "👑 VIP 10" else "👑 VIP $effVip",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Line 3: "entered the room" action subtitle
                    Text(
                        text = if (isApexFounder) "LiveTogather Founder has entered the room. Supreme Authority Active."
                               else if (isApexCeo) "LiveTogather CEO has entered the room. Supreme Leadership Active."
                               else "entered the room",
                        color = if (isApexFounder) Color(0xFFFFD700) else Color(0xFFFFE082).copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 3. Right Icon: Audio / Mic wave button (matching Image 2)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isApexFounder) listOf(Color(0xFFFF1744), Color(0xFF8B1838))
                                else listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                            )
                        )
                        .border(1.dp, BrandGold.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Top Golden Winged Crest with Emblem Number
        EntranceBannerTopCrest(
            emblemText = if (isApexFounder) "👑" else "$effVip",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
        )

        // Bottom Golden Winged Crest with Emblem Number
        EntranceBannerBottomCrest(
            emblemText = if (isApexFounder) "👑" else "$effVip",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 8.dp)
        )

        // 4 Golden Corner Ornaments
        OrnateCornerFlourish(
            isTop = true,
            isLeft = true,
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF8F00),
            gemColor = Color(0xFFFFEA00),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-3).dp, y = (-3).dp)
        )
        OrnateCornerFlourish(
            isTop = true,
            isLeft = false,
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF8F00),
            gemColor = Color(0xFFFFEA00),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 3.dp, y = (-3).dp)
        )
        OrnateCornerFlourish(
            isTop = false,
            isLeft = true,
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF8F00),
            gemColor = Color(0xFFFFEA00),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-3).dp, y = 3.dp)
        )
        OrnateCornerFlourish(
            isTop = false,
            isLeft = false,
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF8F00),
            gemColor = Color(0xFFFFEA00),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 3.dp, y = 3.dp)
        )
    }
}

// -------------------------------------------------------------
// VIP MINI CARD FRAME WRAPPER (For Room Member Profile Bottom Sheet)
// -------------------------------------------------------------
@Composable
fun VipMiniCardFrame(
    isFounder: Boolean = false,
    vipLevel: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isVipOrFounder = isFounder || vipLevel > 0
    if (!isVipOrFounder) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(24.dp),
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            content()
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MiniCardGlow")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val borderBrush = if (isFounder) {
        Brush.sweepGradient(
            listOf(
                Color(0xFFFFD700),
                Color(0xFFFF1744),
                Color(0xFFA855F7),
                Color(0xFF00E5FF),
                Color(0xFFFFD700)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFFFD700),
                Color(0xFFF59E0B),
                Color(0xFF9333EA),
                Color(0xFFFFD700)
            )
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isFounder) Color(0xFF190C29) else Color(0xFF130E22)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFFFFD700))
            .border(2.dp, borderBrush, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Royal Top Ribbon (Matches AhChat Mini Card Plaque)
            Surface(
                color = if (isFounder) Color(0xFF3B124D) else Color(0xFF26133B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isFounder) "👑" else "🛡️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFounder) "LIVETOGATHER FOUNDER" else "VIP PRIVILEGE PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFounder) Color(0xFFFFE082) else Color(0xFFFDE047),
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = if (isFounder) Color(0xFF8B1838) else Color(0xFF0284C7),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = if (isFounder) "SUPREME" else "VIP $vipLevel",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            content()
        }
    }
}

// -------------------------------------------------------------
// VIP CENTER DIALOG (LEVEL DRESSING UP 4-PIECE SUITE)
// -------------------------------------------------------------
@Composable
fun VipCenterDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onOpenTopUp: () -> Unit = {},
    onProfileUpdated: (UserProfile) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTierIndex by remember { mutableIntStateOf(0) }
    var selectedSuitePiece by remember { mutableIntStateOf(3) } // 0: Marquee, 1: Mini Card, 2: Avatar Frame, 3: Chat Frame

    val isFounder = currentUser.isFounder()
    val currentVipLvl = if (isFounder) 10 else currentUser.getVipLevel()
    val activeChatFrame = currentUser.activeChatFrame

    val tiers = listOf(
        Pair("VIP 1", "Silver Knight"),
        Pair("VIP 2", "Golden Paladin"),
        Pair("VIP 3", "Imperial Amethyst"),
        Pair("VIP 4", "Celestial Dragon")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0912)),
            color = Color(0xFF0B0912)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Header (Back + VIP Center Title + Rule Icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFounder) "Founder VIP Center" else "VIP Center",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "VIP privileges unlock with level progression and subscriptions", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Rules", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. 3D Knight Badge Card & Level EXP Bar
                    Surface(
                        color = Color(0xFF191329),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF9333EA)))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isFounder) "⚡" else "🛡️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFounder) "FOUNDER SUPREME" else tiers[selectedTierIndex].first,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isFounder) "Supreme Authority & Architectural Control" else tiers[selectedTierIndex].second,
                                fontSize = 12.sp,
                                color = Color(0xFFE9D5FF)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // EXP Progress Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("EXP: 350 / 500", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("Next: VIP 2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2C2245))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFFFD700), Color(0xFFFF5E62))
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // 2. VIP Tier Horizontal Selector Tabs
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tiers.indices.toList()) { idx ->
                            val isSelected = selectedTierIndex == idx
                            Surface(
                                color = if (isSelected) Color(0xFF381E57) else Color(0xFF181329),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF382B59)
                                ),
                                modifier = Modifier
                                    .clickable { selectedTierIndex = idx }
                            ) {
                                Text(
                                    text = tiers[idx].first,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }

                    // 3. "Level dressing up" (4-Piece Luxury Suite)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Level dressing up",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "4-piece suite",
                                fontSize = 11.5.sp,
                                color = Color(0xFFFFD700)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 Piece Icons: Entrance Marquee, Mini Card, Avatar Frame, Chat Frame
                        val suiteItems = listOf(
                            Triple(0, "🚀", "Entrance"),
                            Triple(1, "📇", "Mini card"),
                            Triple(2, "🖼️", "Frame"),
                            Triple(3, "💬", "Chat frame")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            suiteItems.forEach { (pieceIdx, emoji, label) ->
                                val isPieceSelected = selectedSuitePiece == pieceIdx
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isPieceSelected) Color(0xFF2C1A4A) else Color(0xFF161124))
                                        .border(
                                            width = if (isPieceSelected) 1.5.dp else 1.dp,
                                            color = if (isPieceSelected) Color(0xFFFFD700) else Color(0xFF2D2345),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedSuitePiece = pieceIdx }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isPieceSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isPieceSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Interactive Live Preview & 1-Tap Equip
                    val targetChatFrame = when (selectedTierIndex) {
                        0 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_silver_knight" }
                        1 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_golden_paladin" }
                        2 -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_imperial_amethyst" }
                        else -> VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_celestial_dragon" }
                    } ?: VIP_CHAT_FRAMES_CATALOG[0]

                    val isEquipped = activeChatFrame == targetChatFrame.id

                    Surface(
                        color = Color(0xFF130E22),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFF382B59)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (selectedSuitePiece) {
                                    0 -> "🌟 Entrance Marquee Preview"
                                    1 -> "📇 Personal Profile Mini Card Preview"
                                    2 -> "🖼️ Dynamic Flowing Frame Preview"
                                    else -> "💬 VIP Chat Frame Bubble Preview"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            when (selectedSuitePiece) {
                                3 -> {
                                    // Chat Frame Bubble Preview
                                    VipStyledChatBubble(
                                        messageText = "Your message with gorgeous frame shows your friends",
                                        isMe = true,
                                        chatFrameId = targetChatFrame.id,
                                        vipLevel = selectedTierIndex + 1,
                                        isFounder = isFounder,
                                        timestampText = "Just now"
                                    )
                                }
                                1 -> {
                                    // Mini Card Preview
                                    VipMiniCardFrame(
                                        isFounder = isFounder,
                                        vipLevel = selectedTierIndex + 1,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(currentUser.name.ifEmpty { "VIP Broadcaster" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Luxury 3D profile plaque wrap active", color = Color(0xFFFFD700), fontSize = 11.sp)
                                        }
                                    }
                                }
                                2 -> {
                                    // Avatar Frame Preview
                                    Box(
                                        modifier = Modifier.size(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FlowingAvatarFrame(
                                            photoUrl = currentUser.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                            frameId = if (selectedTierIndex == 0) "frame_silver_knight" else "frame_golden_emperor",
                                            avatarSize = 50.dp,
                                            frameSize = 80.dp
                                        )
                                    }
                                }
                                else -> {
                                    // Entrance Marquee Preview
                                    Surface(
                                        color = Color(0xFF261033),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🚀", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${currentUser.name} entered in a Celestial Chariot! ✨",
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1-Tap Equip Button
                            Button(
                                onClick = {
                                    val newFrameId = if (isEquipped) "" else targetChatFrame.id
                                    db.collection("users").document(currentUser.uid).update(
                                        mapOf(
                                            "activeChatFrame" to newFrameId,
                                            "ownedChatFrames" to (currentUser.ownedChatFrames + targetChatFrame.id).distinct()
                                        )
                                    )
                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("profile_active_chat_frame", newFrameId).apply()
                                    val updated = currentUser.copy(
                                        activeChatFrame = newFrameId,
                                        ownedChatFrames = (currentUser.ownedChatFrames + targetChatFrame.id).distinct()
                                    )
                                    onProfileUpdated(updated)
                                    Toast.makeText(
                                        context,
                                        if (newFrameId.isEmpty()) "Chat frame removed" else "👑 ${targetChatFrame.name} Equipped!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) Color(0xFF16A34A) else Color(0xFFFFD700)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (isEquipped) "Equipped ✓" else "Equip Chat Frame",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    color = if (isEquipped) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    // 5. Privileges List (Matches AhChat)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Level Privileges",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val privs = listOf(
                            Pair("👑", "Exclusive VIP Badge & Crown across all rooms"),
                            Pair("💬", "Custom 3D Beveled Crystal Chat Frame"),
                            Pair("📇", "Royal Mini Card Plaque Frame on Profile Sheet"),
                            Pair("🚀", "Room Entrance Marquee Banner with Star Trail"),
                            Pair("💎", "10% Extra Diamond Bonus on Gifts received"),
                            Pair("🎙️", "Priority High-Def Voice Streaming")
                        )

                        privs.forEach { (icon, text) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF1F1735),
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(icon, fontSize = 13.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text, color = Color.White.copy(alpha = 0.85f), fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MONTHLY CARD DIALOG (MYSTIC SCORPIO 5x5 TILE PUZZLE & DRAW)
// -------------------------------------------------------------
@Composable
fun MonthlyCardDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var claimState by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070510)),
            color = Color(0xFF070510)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "Monthly Card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Draw daily cards to collect fragments for the Mystic Scorpio frame!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Rules", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Mystic Scorpio Banner Plaque
                    Surface(
                        color = Color(0xFF181033),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC)))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🦂", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MYSTIC SCORPIO",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA5B4FC),
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "30-Day Zodiac Seasonal Pass • 5x5 Matrix Draw",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // 5x5 Matrix Draw Tiles Showcase (Direct AhChat Recreation)
                    Surface(
                        color = Color(0xFF120C24),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF382B59)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Draw Matrix (Daily Free 1x Unlock)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 5 Rows of 5 Tiles
                            val tileIcons = listOf(
                                "🦂", "💎", "💍", "✨", "👑",
                                "💬", "🎁", "🦂", "⭐", "💎",
                                "💍", "🔮", "⚡", "🦂", "💬",
                                "💎", "⭐", "👑", "✨", "💍",
                                "🦂", "💬", "💎", "🎁", "🦂"
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(tileIcons.indices.toList()) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (idx % 3 == 0) Color(0xFF311E52) else Color(0xFF1B1430)
                                            )
                                            .border(
                                                1.dp,
                                                if (idx % 3 == 0) Color(0xFFA855F7) else Color(0xFF382B59),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tileIcons[idx], fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Draw / Claim Action Button
                    Button(
                        onClick = {
                            claimState = true
                            Toast.makeText(context, "🎉 Unlocked Mystic Scorpio Chat Frame for 7 Days!", Toast.LENGTH_LONG).show()
                            db.collection("users").document(currentUser.uid).update(
                                mapOf(
                                    "activeChatFrame" to "chat_frame_mystic_scorpio",
                                    "ownedChatFrames" to (currentUser.ownedChatFrames + "chat_frame_mystic_scorpio").distinct()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (claimState) "Claimed Today ✓" else "Draw 1x (Free Today)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // Seasonal Rewards Preview
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Seasonal Grand Rewards",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val rewards = listOf(
                            Triple("🦂", "Mystic Scorpio Flowing Frame", "30 Days Permanent"),
                            Triple("💬", "Constellation Scorpio Chat Frame", "Exclusive Bubble"),
                            Triple("💍", "Scorpio Intimacy Couple Ring", "Permanent CP Ring"),
                            Triple("💎", "10,000 Seasonal Diamonds", "Direct Credit")
                        )

                        rewards.forEach { (emoji, title, desc) ->
                            Surface(
                                color = Color(0xFF140F26),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2A1F45)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text(desc, color = Color(0xFFA5B4FC), fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
