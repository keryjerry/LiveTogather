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
        borderBrush = Brush.horizontalGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFFE2E8F0))
        ),
        backgroundBrush = Brush.verticalGradient(
            listOf(Color(0xFF131D2E), Color(0xFF0B121C))
        ),
        textColor = Color(0xFFF8FAFC),
        accentColor = Color(0xFF94A3B8),
        secondaryAccentColor = Color(0xFFE2E8F0),
        gemColor = Color(0xFFCBD5E1),
        cornerWingColor = Color(0xFF94A3B8),
        badgeLabel = "VIP 1",
        badgeBgColor = Color(0xFF334155),
        cornerGemEmoji = "💎",
        description = "Beveled silver crystal bubble with winged steel crest for VIP 1"
    ),
    VipChatFrameItem(
        id = "chat_frame_golden_paladin",
        name = "Golden Paladin (VIP 2)",
        vipLevel = 2,
        borderBrush = Brush.horizontalGradient(
            listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFFD700), Color(0xFFFFE082))
        ),
        backgroundBrush = Brush.verticalGradient(
            listOf(Color(0xFF2E1C07), Color(0xFF140B02))
        ),
        textColor = Color(0xFFFFFBEB),
        accentColor = Color(0xFFFFD700),
        secondaryAccentColor = Color(0xFFFF8F00),
        gemColor = Color(0xFFFFEA00),
        cornerWingColor = Color(0xFFFFD700),
        badgeLabel = "VIP 2",
        badgeBgColor = Color(0xFFB45309),
        cornerGemEmoji = "\u2b50",
        description = "Regal golden baroque frame with imperial amber glow for VIP 2"
    ),
    VipChatFrameItem(
        id = "chat_frame_imperial_amethyst",
        name = "Imperial Amethyst (VIP 3)",
        vipLevel = 3,
        borderBrush = Brush.horizontalGradient(
            listOf(Color(0xFFE879F9), Color(0xFFA855F7), Color(0xFFFFD700), Color(0xFFE879F9))
        ),
        backgroundBrush = Brush.verticalGradient(
            listOf(Color(0xFF2C103A), Color(0xFF14061C))
        ),
        textColor = Color(0xFFFAF5FF),
        accentColor = Color(0xFFE879F9),
        secondaryAccentColor = Color(0xFFFFD700),
        gemColor = Color(0xFFA855F7),
        cornerWingColor = Color(0xFFE879F9),
        badgeLabel = "VIP 3",
        badgeBgColor = Color(0xFF7E22CE),
        cornerGemEmoji = "🔮",
        description = "Royal amethyst crystal with gold embellishments for VIP 3"
    ),
    VipChatFrameItem(
        id = "chat_frame_celestial_dragon",
        name = "Celestial Dragon (VIP 4+)",
        vipLevel = 4,
        borderBrush = Brush.horizontalGradient(
            listOf(Color(0xFFFFD700), Color(0xFFFF2A55), Color(0xFFFF7A00), Color(0xFFFFD700))
        ),
        backgroundBrush = Brush.verticalGradient(
            listOf(Color(0xFF26081B), Color(0xFF13040E))
        ),
        textColor = Color(0xFFFFF1F2),
        accentColor = Color(0xFFFF2A55),
        secondaryAccentColor = Color(0xFFFFD700),
        gemColor = Color(0xFFFFD700),
        cornerWingColor = Color(0xFFFF7A00),
        badgeLabel = "VIP 👑",
        badgeBgColor = Color(0xFF991B1B),
        cornerGemEmoji = "🐉",
        description = "Celestial Dragon Ruby & 24K Gold velvet bubble with 3D winged sword crest"
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
    if (!frameId.isNullOrBlank()) {
        val found = VIP_CHAT_FRAMES_CATALOG.find { it.id == frameId }
        if (found != null) return found
    }
    if (vipLevel in 1..4) {
        val tierFrameId = when (vipLevel) {
            4 -> "chat_frame_celestial_dragon"
            3 -> "chat_frame_imperial_amethyst"
            2 -> "chat_frame_golden_paladin"
            1 -> "chat_frame_silver_knight"
            else -> null
        }
        if (tierFrameId != null) {
            val found = VIP_CHAT_FRAMES_CATALOG.find { it.id == tierFrameId }
            if (found != null) return found
        }
    }
    if (isFounder && vipLevel == 0 && frameId.isNullOrBlank()) {
        return VIP_CHAT_FRAMES_CATALOG.find { it.id == "chat_frame_founder_supreme" }
    }
    return null
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

            val swordRes = when (frameItem.vipLevel) {
                1 -> R.drawable.vip1_chat_sword_master
                2 -> R.drawable.vip2_chat_sword_master
                3 -> R.drawable.vip3_chat_sword_master
                4 -> R.drawable.vip4_chat_sword_master
                else -> 0
            }

            if (swordRes != 0) {
                Image(
                    painter = painterResource(id = swordRes),
                    contentDescription = "VIP Sword Crest",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 6.dp, y = (-12).dp)
                        .size(96.dp, 26.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Top Center Ornate Winged Crest
                OrnateTopCrest(
                    primaryColor = frameItem.secondaryAccentColor,
                    accentColor = frameItem.accentColor,
                    gemColor = frameItem.gemColor,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-4).dp)
                )
            }

            if (swordRes == 0) {
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
                    frameId = run {
                        val af = userProfile?.activeFrame ?: ""
                        if (af.isNotBlank()) af else if (isApexFounder) "frame_livetogather_founder" else "frame_imperial_gold"
                    },
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
// -------------------------------------------------------------
// VIP CENTER DIALOG (MATCHING OFFICIAL REFERENCE ARCHITECTURE)
// -------------------------------------------------------------
data class VipSuiteTierModel(
    val level: Int,
    val name: String,
    val subtitle: String,
    val expText: String,
    val inrPrice: Long,
    val formattedPrice: String,
    val avatarRes: Int,
    val bubbleRes: Int,
    val swordRes: Int,
    val archRes: Int,
    val coverRes: Int,
    val mockupRes: Int,
    val frameId: String,
    val chatFrameId: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

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
    var selectedSuitePiece by remember { mutableIntStateOf(2) } // 0: Bar, 1: Profile Show, 2: Avatar Frame, 3: Chat Frame
    var showEnlargedPreview by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    var showPaymentModal by remember { mutableStateOf(false) }
    var checkoutPackage by remember { mutableStateOf<CoinPackageItem?>(null) }
    val isFounder = currentUser.isFounder()

    val tiers = remember {
        listOf(
            VipSuiteTierModel(
                level = 1,
                name = "VIP 1",
                subtitle = "Frost Valkyrie & Silver Knight",
                expText = "Exp 1000",
                inrPrice = 2500L,
                formattedPrice = "₹2,500",
                avatarRes = R.drawable.vip1_avatar_master,
                bubbleRes = R.drawable.vip1_bubble_master,
                swordRes = R.drawable.vip1_chat_sword_master,
                archRes = R.drawable.vip1_header_arch_master,
                coverRes = R.drawable.vip1_cover_master,
                mockupRes = R.drawable.vip1_profile_mockup,
                frameId = "vip_suite_frame_1",
                chatFrameId = "chat_frame_silver_knight",
                primaryColor = Color(0xFF00E5FF),
                secondaryColor = Color(0xFFE2E8F0)
            ),
            VipSuiteTierModel(
                level = 2,
                name = "VIP 2",
                subtitle = "Solar Phoenix & Golden Paladin",
                expText = "Exp 2500",
                inrPrice = 5000L,
                formattedPrice = "₹5,000",
                avatarRes = R.drawable.vip2_avatar_master,
                bubbleRes = R.drawable.vip2_bubble_master,
                swordRes = R.drawable.vip2_chat_sword_master,
                archRes = R.drawable.vip2_header_arch_master,
                coverRes = R.drawable.vip2_cover_master,
                mockupRes = R.drawable.vip2_profile_mockup,
                frameId = "vip_suite_frame_2",
                chatFrameId = "chat_frame_golden_paladin",
                primaryColor = Color(0xFFFFD700),
                secondaryColor = Color(0xFFFF8F00)
            ),
            VipSuiteTierModel(
                level = 3,
                name = "VIP 3",
                subtitle = "Imperial Amethyst Monarch",
                expText = "Exp 5000",
                inrPrice = 15000L,
                formattedPrice = "₹15,000",
                avatarRes = R.drawable.vip3_avatar_master,
                bubbleRes = R.drawable.vip3_bubble_master,
                swordRes = R.drawable.vip3_chat_sword_master,
                archRes = R.drawable.vip3_header_arch_master,
                coverRes = R.drawable.vip3_cover_master,
                mockupRes = R.drawable.vip3_profile_mockup,
                frameId = "vip_suite_frame_3",
                chatFrameId = "chat_frame_imperial_amethyst",
                primaryColor = Color(0xFFA855F7),
                secondaryColor = Color(0xFFFFD700)
            ),
            VipSuiteTierModel(
                level = 4,
                name = "VIP 4",
                subtitle = "Sovereign Celestial Dragon",
                expText = "Exp 10000",
                inrPrice = 20000L,
                formattedPrice = "₹20,000",
                avatarRes = R.drawable.vip4_avatar_master,
                bubbleRes = R.drawable.vip4_bubble_master,
                swordRes = R.drawable.vip4_chat_sword_master,
                archRes = R.drawable.vip4_header_arch_master,
                coverRes = R.drawable.vip4_cover_master,
                mockupRes = R.drawable.vip4_profile_mockup,
                frameId = "vip_suite_frame_4",
                chatFrameId = "chat_frame_celestial_dragon",
                primaryColor = Color(0xFFFF1744),
                secondaryColor = Color(0xFFFFD700)
            )
        )
    }

    val currentTier = tiers[selectedTierIndex]

    // Ownership & Equipped Checks - Strictly for the specific purchased tier!
    val isOwned = (currentUser.getVipLevel() == currentTier.level) ||
        currentUser.plan.equals("VIP ${currentTier.level}", ignoreCase = true)

    val isEquipped = currentUser.activeFrame == currentTier.frameId &&
        currentUser.activeChatFrame == currentTier.chatFrameId

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (showPaymentModal && checkoutPackage != null) {
            UpiPaymentCheckoutModal(
                currentUser = currentUser,
                selectedPackage = checkoutPackage!!,
                onDismiss = {
                    showPaymentModal = false
                    checkoutPackage = null
                },
                onSuccess = {
                    showPaymentModal = false
                    val pkg = checkoutPackage
                    checkoutPackage = null
                    // Direct 1-tap activation on successful purchase
                    val newPlan = "VIP ${currentTier.level}"
                    val newFrame = currentTier.frameId
                    val newChatFrame = currentTier.chatFrameId
                    val newChatBar = "chat_sword_vip_${currentTier.level}"
                    val newArch = "arch_vip_${currentTier.level}"
                    val newCover = "cover_vip_${currentTier.level}"
                    val newEntrance = "entrance_vip_${currentTier.level}"

                    val updates = mapOf(
                        "plan" to newPlan,
                        "vipLevel" to currentTier.level,
                        "activeFrame" to newFrame,
                        "activeChatFrame" to newChatFrame,
                        "activeChatBar" to newChatBar,
                        "activeProfileArch" to newArch,
                        "activeProfileCover" to newCover,
                        "activeEntranceEffect" to newEntrance,
                        "ownedFrames" to (currentUser.ownedFrames + newFrame).distinct(),
                        "ownedChatFrames" to (currentUser.ownedChatFrames + newChatFrame).distinct()
                    )
                    db.collection("users").document(currentUser.uid).update(updates)

                    val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("profile_active_frame", newFrame)
                        .putString("profile_active_chat_frame", newChatFrame)
                        .putString("profile_active_plan", newPlan)
                        .apply()

                    val updatedUser = currentUser.copy(
                        plan = newPlan,
                        activeFrame = newFrame,
                        activeChatFrame = newChatFrame,
                        ownedFrames = (currentUser.ownedFrames + newFrame).distinct(),
                        ownedChatFrames = (currentUser.ownedChatFrames + newChatFrame).distinct()
                    )
                    onProfileUpdated(updatedUser)
                    Toast.makeText(context, "🎉 ${currentTier.name} Dressing Up Suite Activated!", Toast.LENGTH_LONG).show()
                }
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08060D)),
            color = Color(0xFF08060D)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    // 1. Top Bar: Back Button, Title, Rule Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF181324), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = "VIP Center",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "VIP privileges unlock with level progression and official direct subscriptions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF181324), CircleShape)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Rules", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        }
                    }

                    // 2. Scrollable Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 2A. Hero 3D Badge & Spotlight Platform (Spacious & Clean)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Spotlight Glow
                            Box(
                                modifier = Modifier
                                    .size(180.dp, 80.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                currentTier.primaryColor.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // 3D Guardian Knight Badge
                                Image(
                                    painter = painterResource(id = currentTier.avatarRes),
                                    contentDescription = currentTier.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(118.dp)
                                        .clickable { showEnlargedPreview = true }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = currentTier.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    text = currentTier.expText,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    color = Color(0xFF1E1728),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💎", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Price: ${currentTier.formattedPrice} / 30 Days",
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFF16A34A),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "DIRECT INR",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2B. Horizontal VIP Tier Tabs ([VIP 1], [VIP 2], [VIP 3], [VIP 4])
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tiers.forEachIndexed { index, tier ->
                                val isSelected = selectedTierIndex == index
                                Surface(
                                    color = if (isSelected) Color(0xFF281C3D) else Color(0xFF130E20),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color(0xFF2C2042)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTierIndex = index }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = tier.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        // 2C. "Level dressing up" Section (Matching Reference 4-Card Row)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Section Header with wings
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.6f)))))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Level dressing up",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.6f), Color.Transparent))))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4-Card Row with Play Indicator ▶
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cards = listOf(
                                    Triple(0, currentTier.swordRes, "Room Bar"),
                                    Triple(1, currentTier.mockupRes, "Profile Show"),
                                    Triple(2, currentTier.avatarRes, "Avatar Frame"),
                                    Triple(3, currentTier.bubbleRes, "Chat Frame")
                                )

                                cards.forEach { (idx, resId, label) ->
                                    val isCardSelected = selectedSuitePiece == idx
                                    Surface(
                                        color = Color(0xFF141022),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            width = if (isCardSelected) 1.5.dp else 1.dp,
                                            color = if (isCardSelected) Color(0xFFFFD700) else Color(0xFF28203D)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(84.dp)
                                            .clickable {
                                                selectedSuitePiece = idx
                                                showEnlargedPreview = true
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            // Circular Play Indicator (▶)
                                            Surface(
                                                color = Color(0xFFFFB300),
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .padding(3.dp)
                                                    .align(Alignment.TopStart)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.PlayArrow,
                                                        contentDescription = "Preview",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }

                                            // 3D Master Graphic
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = label,
                                                contentScale = if (idx == 0) ContentScale.Fit else ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(top = 14.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2D. "Privileges (17/52)" Grid Section
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)))))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Privileges (17/52)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent))))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val privilegesList = listOf(
                                Pair("💬", "Chat Without Restrictions"),
                                Pair("🛡️", "VIP Badge"),
                                Pair("📇", "VIP Mini Card"),
                                Pair("🖼️", "Unlimited Avatar Frame Switch"),
                                Pair("🎁", "Exclusive VIP Gifts"),
                                Pair("🌟", "Profile Show"),
                                Pair("💬", "Custom VIP Chat Frame"),
                                Pair("✉️", "Unlimited DMs"),
                                Pair("👁️", "All Visitors"),
                                Pair("✉️", "Read Receipts"),
                                Pair("🎨", "Customize Avatar"),
                                Pair("🎙️", "Exclusive Stickers on MIC"),
                                Pair("🏰", "VIP Room Background"),
                                Pair("🔄", "Exclusive Avatar Switch"),
                                Pair("🚀", "Special Enter Message"),
                                Pair("✨", "Gift Track"),
                                Pair("🛡️", "Mic & Room Anti-Kick Protection")
                            )

                            // 3-Column Metallic Icon Grid
                            val chunkedPrivs = privilegesList.chunked(3)
                            chunkedPrivs.forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    rowItems.forEach { (emoji, title) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(96.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFF1E1728),
                                                shape = CircleShape,
                                                border = BorderStroke(1.dp, Color(0xFF5A4632)),
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(emoji, fontSize = 20.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = title,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.White.copy(alpha = 0.75f),
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(60.dp)) // Clearance for bottom fixed bar
                    }
                }

                // 3. Fixed Bottom Action Bar:
                // Strictly ONLY Purchase button if not purchased!
                // Once purchased, shows Wear (वियर) and Unwear (अनवियर) button!
                val hasPurchasedTier = (currentUser.getVipLevel() == currentTier.level) ||
                    currentUser.plan.equals("VIP ${currentTier.level}", ignoreCase = true)

                val isWorn = currentUser.activeFrame == currentTier.frameId &&
                    currentUser.activeChatFrame == currentTier.chatFrameId

                Surface(
                    color = Color(0xFF08060D),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!hasPurchasedTier) {
                            // NOT PURCHASED YET: Show ONLY the Purchase Button!
                            Button(
                                onClick = {
                                    checkoutPackage = CoinPackageItem(
                                        id = "vip_package_${currentTier.level}",
                                        coins = 0L,
                                        priceInr = currentTier.formattedPrice,
                                        numericPrice = currentTier.inrPrice.toString(),
                                        title = "LiveTogather ${currentTier.name} Dressing Up Suite (30 Days)",
                                        badge = currentTier.name
                                    )
                                    showPaymentModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                shape = RoundedCornerShape(26.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("💳", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Purchase ${currentTier.name} • ${currentTier.formattedPrice} / 30 Days",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1B1433)
                                    )
                                }
                            }
                        } else {
                            // PURCHASED: Show Wear (वियर) / Unwear (अनवियर) Toggle Button!
                            Button(
                                onClick = {
                                    if (isWorn) {
                                        // Unwear: Remove VIP dressing up suite
                                        val updates = mapOf(
                                            "activeFrame" to "",
                                            "activeChatFrame" to "",
                                            "activeChatBar" to "",
                                            "activeProfileArch" to "",
                                            "activeProfileCover" to "",
                                            "activeEntranceEffect" to ""
                                        )
                                        db.collection("users").document(currentUser.uid).update(updates)
                                        val updatedUser = currentUser.copy(
                                            activeFrame = "",
                                            activeChatFrame = ""
                                        )
                                        onProfileUpdated(updatedUser)
                                        Toast.makeText(context, "VIP Suite Unworn (हटा दिया गया)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Wear: Equip entire VIP suite
                                        val newPlan = "VIP ${currentTier.level}"
                                        val newFrame = currentTier.frameId
                                        val newChatFrame = currentTier.chatFrameId
                                        val newChatBar = "chat_sword_vip_${currentTier.level}"
                                        val newArch = "arch_vip_${currentTier.level}"
                                        val newCover = "cover_vip_${currentTier.level}"
                                        val newEntrance = "entrance_vip_${currentTier.level}"

                                        val updates = mapOf(
                                            "plan" to newPlan,
                                            "vipLevel" to currentTier.level,
                                            "activeFrame" to newFrame,
                                            "activeChatFrame" to newChatFrame,
                                            "activeChatBar" to newChatBar,
                                            "activeProfileArch" to newArch,
                                            "activeProfileCover" to newCover,
                                            "activeEntranceEffect" to newEntrance
                                        )
                                        db.collection("users").document(currentUser.uid).update(updates)
                                        val updatedUser = currentUser.copy(
                                            plan = newPlan,
                                            activeFrame = newFrame,
                                            activeChatFrame = newChatFrame
                                        )
                                        onProfileUpdated(updatedUser)
                                        Toast.makeText(context, "👑 ${currentTier.name} Suite Worn (वियर हो गया)!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWorn) Color(0xFFEF4444) else Color(0xFF16A34A)
                                ),
                                shape = RoundedCornerShape(26.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = if (isWorn) "✕ Unwear (अनवियर करें)" else "👑 Wear (वियर करें)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Small secondary renew button
                            OutlinedButton(
                                onClick = {
                                    checkoutPackage = CoinPackageItem(
                                        id = "vip_package_${currentTier.level}",
                                        coins = 0L,
                                        priceInr = currentTier.formattedPrice,
                                        numericPrice = currentTier.inrPrice.toString(),
                                        title = "LiveTogather ${currentTier.name} Dressing Up Suite (30 Days)",
                                        badge = currentTier.name
                                    )
                                    showPaymentModal = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Renew Subscription • ${currentTier.formattedPrice}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // 4. Enlarged 3D Preview Modal (When Tapping Any Card)
                if (showEnlargedPreview) {
                    Dialog(onDismissRequest = { showEnlargedPreview = false }) {
                        Surface(
                            color = Color(0xFF110C1E),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedSuitePiece) {
                                        0 -> "⚔️ ${currentTier.name} Room Chat Bar with Royal Sword"
                                        1 -> "👑 ${currentTier.name} Profile Show & Palace Cover"
                                        2 -> "🖼️ ${currentTier.name} 3D Flowing Avatar Frame"
                                        else -> "💬 ${currentTier.name} Ultra-HD Chat Frame"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val previewRes = when (selectedSuitePiece) {
                                    0 -> currentTier.swordRes
                                    1 -> currentTier.mockupRes
                                    2 -> currentTier.avatarRes
                                    else -> currentTier.bubbleRes
                                }

                                Image(
                                    painter = painterResource(id = previewRes),
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showEnlargedPreview = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2248)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Close Preview", color = Color.White)
                                }
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
