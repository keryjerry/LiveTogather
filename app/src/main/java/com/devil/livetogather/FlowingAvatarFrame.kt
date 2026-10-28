package com.devil.livetogather

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.viewinterop.AndroidView

/**
 * High-performance Flowing Avatar Frame Composable.
 * Ensures the frame rests on the OUTER boundary of the profile picture
 * so the user's face is 100% visible, crisp, and never cut off.
 */
@Composable
fun FlowingAvatarFrame(
    photoUrl: String,
    frameId: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 46.dp,
    frameSize: Dp = 86.dp,
    isSpeaking: Boolean = false,
    vipLevel: Int = 0,
    showAnimation: Boolean = true,
    showVipBadge: Boolean = true,
    showMicEffect: Boolean = false,
    micEffectId: String = "",
    onClick: (() -> Unit)? = null
) {
    val frameItem = AVATAR_FRAMES_CATALOG.find { it.id == frameId }
    val effectiveVip = if (vipLevel > 0) vipLevel else (frameItem?.vipLevel ?: 0)
    val isAnimated = showAnimation && (frameItem?.isAnimated ?: frameId.isNotBlank())
    val drawableRes = frameItem?.drawableRes ?: getAvatarFrameDrawable(frameId)

    // Smooth Infinite Animation Controller for Flowing Effects
    val infiniteTransition = rememberInfiniteTransition(label = "FlowingFrameTransition")

    // 1. Fast Dual-Speed Rotating Aura — outer slow, inner fast for depth illusion
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )
    val rotateAngleInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotationInner"
    )

    // 2. Smooth Organic Wing Flap — EaseInOutSine gives natural bird-flight feel
    val wingLift by infiniteTransition.animateFloat(
        initialValue = if (isAnimated) -4.5f else 0f,
        targetValue = if (isAnimated) 4.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WingLift"
    )
    val wingSkew by infiniteTransition.animateFloat(
        initialValue = if (isAnimated) -1.5f else 0f,
        targetValue = if (isAnimated) 1.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WingSkew"
    )

    // 3. Gentle Living Pulse — shimmer-breathe effect on frame border
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isAnimated) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FramePulse"
    )

    // 4. Shimmering Alpha for aura ring — makes it truly "live"
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = if (isAnimated) 1.0f else 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    // Aura Color Palette derived from frame
    val auraColorList = frameItem?.auraColors?.map { Color(it) } ?: listOf(
        BrandGold,
        Color(0xFFFFB300),
        BrandCoral,
        BrandGold
    )

    // Universal Dynamic Golden Ratio Calibration: Automatically matches inner hole bounds of any frame
    val minScale = when (drawableRes) {
        R.drawable.vip1_avatar_master -> 2.34f
        R.drawable.vip2_avatar_master -> 2.57f
        R.drawable.vip3_avatar_master -> 2.86f
        R.drawable.vip4_avatar_master -> 2.83f
        R.drawable.frame_livetogather_founder -> 2.27f
        else -> 1.86f
    }

    // Identifies VIP Suite frames (these skip the avatar border ring and VIP badge, frame itself has VIP indicator)
    val isVipSuiteFrame = drawableRes in listOf(
        R.drawable.vip1_avatar_master,
        R.drawable.vip2_avatar_master,
        R.drawable.vip3_avatar_master,
        R.drawable.vip4_avatar_master
    )

    val effectiveFrameSize = if (frameId.isNotBlank()) {
        avatarSize * minScale
    } else if (micEffectId.isNotBlank() && isSpeaking) {
        avatarSize + 16.dp
    } else {
        avatarSize
    }

    BoxWithConstraints(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val maxBound = if (maxWidth != Dp.Infinity && maxWidth > 0.dp) maxWidth else effectiveFrameSize
        val targetFrameSize = if (frameId.isNotBlank()) {
            if (maxBound > 0.dp && maxBound < effectiveFrameSize) maxBound else effectiveFrameSize
        } else {
            effectiveFrameSize
        }
        val targetAvatarSize = if (frameId.isNotBlank()) {
            (targetFrameSize / minScale).coerceAtLeast(28.dp)
        } else {
            avatarSize
        }

        Box(
            modifier = Modifier.size(targetFrameSize),
            contentAlignment = Alignment.Center
        ) {
            // 3D Transparent Video Magic Mic Effect (Strictly ONLY when actively equipped AND user is actively speaking)
            val activeVideoRes = if (micEffectId.isNotBlank() && isSpeaking) {
                getMicEffectVideoRes(micEffectId)
            } else {
                0
            }

            // Speech Pulsing Wave (used when actively speaking without video mic effect)
            if (isSpeaking && activeVideoRes == 0) {
                Box(
                    modifier = Modifier
                        .size(targetAvatarSize + 12.dp)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = 0.3f))
                        .border(1.5.dp, BrandGreen, CircleShape)
                )
            }

            if (activeVideoRes != 0) {
                VideoMicMagicEffectOverlay(
                    videoRes = activeVideoRes,
                    avatarSize = targetAvatarSize,
                    effectSize = targetAvatarSize + 16.dp
                )
            }

            // Flowing Rotating Aura Ring (Living motion graphics shining behind frame)
            if (isAnimated && frameId.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(targetAvatarSize + 7.dp)
                        .graphicsLayer(
                            rotationZ = rotateAngle,
                            alpha = auraAlpha
                        )
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(auraColorList),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(targetAvatarSize + 3.dp)
                        .graphicsLayer(
                            rotationZ = rotateAngleInner,
                            alpha = auraAlpha * 0.55f
                        )
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            brush = Brush.sweepGradient(auraColorList.reversed()),
                            shape = CircleShape
                        )
                )
            }

            // BASE LAYER: Uncropped, Clear Profile Picture (Fits snugly into frame hole without double border)
            Box(
                modifier = Modifier
                    .size(targetAvatarSize)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .then(
                        if (!isVipSuiteFrame) {
                            Modifier.border(
                                width = 1.5.dp,
                                color = if (isSpeaking) {
                                    if (frameId.isBlank()) Color(0xFF00E5FF) else BrandGreen
                                } else if (frameId.isNotBlank()) {
                                    auraColorList.first().copy(alpha = 0.8f)
                                } else {
                                    Color(0x5500E5FF)
                                },
                                shape = CircleShape
                            )
                        } else Modifier
                    )
            ) {
                AsyncImage(
                    model = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // OVERLAY LAYER: Outer Winged Frame (100% Rock-Solid Stable Centering & Smooth Flowing Wings)
            if (frameId.isNotBlank()) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = frameItem?.name ?: "Avatar Frame",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationY = wingLift,
                            rotationZ = wingSkew * 0.4f,
                            scaleX = pulseScale,
                            scaleY = pulseScale
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // VIP Level Badge (if user has VIP level and showVipBadge is true)
            if (showVipBadge && effectiveVip > 0 && !isVipSuiteFrame) {
                val (badgeText, badgeBg) = when (effectiveVip) {
                    1 -> "VIP 1" to Color(0xFFC0C0C0) // Silver
                    2 -> "VIP 2" to BrandGold        // Gold
                    3 -> "VIP 3" to Color(0xFF00D4FF) // Sapphire Blue
                    4 -> "VIP 4" to Color(0xFF00E5FF) // Cyan Blade
                    5 -> "VIP 5" to Color(0xFF00E676) // Emerald Knight
                    6 -> "VIP 6" to Color(0xFFFFD700) // Imperial Eagles
                    else -> "VIP $effectiveVip" to BrandGold
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "👑 $badgeText",
                        color = badgeBg,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Compact Grid Thumbnail Box for Frame Store.
 * Shows a live running animation preview inside the small thumbnail box!
 */
@Composable
fun CompactFrameThumbnailBox(
    frameItem: AvatarFrameItem,
    isEquipped: Boolean,
    isOwned: Boolean,
    userPhotoUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expiryTimestamp: Long = 0L
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThumbnailTransition")
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ThumbRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (frameItem.isAnimated) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThumbPulse"
    )

    val auraColors = frameItem.auraColors.map { Color(it) }

    val now = System.currentTimeMillis()
    val isExpired = isOwned && expiryTimestamp > 0L && now > expiryTimestamp
    val remainingMs = if (isOwned && expiryTimestamp > 0L) expiryTimestamp - now else 0L
    val validityLabel = if (isOwned && expiryTimestamp > 0L) {
        if (remainingMs <= 0L) {
            "Expired"
        } else {
            val days = (remainingMs / (1000L * 60 * 60 * 24)).toInt()
            if (days > 1) {
                "${days}d left"
            } else {
                val hours = (remainingMs / (1000L * 60 * 60)).toInt()
                if (hours >= 1) {
                    "${hours}h left"
                } else {
                    val mins = (remainingMs / (1000L * 60)).toInt().coerceAtLeast(1)
                    "${mins}m left"
                }
            }
        }
    } else ""

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEquipped) Color(0xFF1E3A24)
                else if (isOwned && !isExpired) Color(0xFF2A2215)
                else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = if (isEquipped) 1.5.dp else 1.dp,
                color = if (isEquipped) BrandGreen else if (isOwned && !isExpired) BrandGold else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Live Running Frame Mini-Preview (Centered)
            Box(
                modifier = Modifier.size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated sweep aura if animated
                if (frameItem.isAnimated) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .graphicsLayer(rotationZ = rotateAngle)
                            .clip(CircleShape)
                            .border(1.5.dp, Brush.sweepGradient(auraColors), CircleShape)
                    )
                }

                // Avatar inside
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .border(1.dp, auraColors.first().copy(alpha = 0.6f), CircleShape)
                ) {
                    AsyncImage(
                        model = userPhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = "Avatar Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Frame Overlay with gentle breathing
                Image(
                    painter = painterResource(id = frameItem.drawableRes),
                    contentDescription = frameItem.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Frame Name (1 line, ellipsis)
            Text(
                text = frameItem.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Price / Status Badge with Live Countdown
            if (isEquipped) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandGreen.copy(alpha = 0.2f))
                        .border(0.8.dp, BrandGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (validityLabel.isNotBlank()) "âœ“ Applied ($validityLabel)" else "âœ“ Applied",
                        color = BrandGreen,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isOwned && !isExpired) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandGold.copy(alpha = 0.2f))
                        .border(0.8.dp, BrandGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (validityLabel.isNotBlank()) "Apply ($validityLabel)" else "Apply",
                        color = BrandGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isExpired) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Red.copy(alpha = 0.2f))
                        .border(0.8.dp, Color.Red.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("Expired â€¢ Rebuy", color = Color(0xFFFF6B6B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                val formattedCost = formatDiamondCost(frameItem.cost)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandCoral.copy(alpha = 0.2f))
                        .border(0.8.dp, BrandCoral.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("ðŸ’Ž $formattedCost (1 Mo)", color = BrandCoral, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Compact Video Gift Thumbnail with Live Animated Preview.
 */
@Composable
fun CompactVideoGiftThumbnailBox(
    gift: VideoGiftItem,
    canAfford: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GiftTransition")

    // Dynamic animation based on gift type
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GiftPulse"
    )

    val carSlideOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CarSlide"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GiftRotate"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF332048) else Color.White.copy(alpha = 0.05f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BrandGold else if (canAfford) BrandGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BrandGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = BrandDark,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Live Running Animation Preview
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(
                        1.dp,
                        if (canAfford) Brush.sweepGradient(listOf(BrandGold, BrandCoral, BrandGold))
                        else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (gift.thumbnailResId > 0) {
                    Image(
                        painter = painterResource(id = gift.thumbnailResId),
                        contentDescription = gift.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    when (gift.animationType) {
                        "car" -> {
                            Text(
                                text = gift.emoji,
                                fontSize = 26.sp,
                                modifier = Modifier.offset(x = carSlideOffset.dp)
                            )
                        }
                        "lion" -> {
                            Text(
                                text = gift.emoji,
                                fontSize = 28.sp,
                                modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                            )
                        }
                        "dragon" -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer(rotationZ = rotateAngle)
                            ) {
                                Text(text = "ðŸŒ€", fontSize = 32.sp)
                            }
                            Text(text = gift.emoji, fontSize = 24.sp)
                        }
                        "rocket" -> {
                            Text(
                                text = gift.emoji,
                                fontSize = 26.sp,
                                modifier = Modifier.offset(y = (-carSlideOffset).dp)
                            )
                        }
                        else -> {
                            Text(
                                text = gift.emoji,
                                fontSize = 26.sp,
                                modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Gift Name
            Text(
                text = gift.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Diamond Price
            val formattedCost = formatDiamondCost(gift.cost)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canAfford) BrandGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                    .border(0.8.dp, if (canAfford) BrandGold.copy(alpha = 0.6f) else Color.Gray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ðŸ’Ž $formattedCost",
                    color = if (canAfford) BrandGold else Color.LightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Compact Diamond formatter e.g. 250000 -> 2.5L, 75000 -> 75K, 1000000 -> 10L / 1M.
 */
fun formatDiamondCost(cost: Long): String {
    return when {
        cost >= 10000000L -> "%.1fCr".format(cost / 10000000.0)
        cost >= 100000L -> "%.1fL".format(cost / 100000.0).replace(".0L", "L")
        cost >= 1000L -> "${cost / 1000}K"
        else -> cost.toString()
    }
}


// ---------------------------------------------------------------------
// 3D TRANSPARENT VIDEO MAGIC MIC EFFECT OVERLAY
// ---------------------------------------------------------------------

fun getMicEffectVideoRes(effectId: String): Int {
    return when (effectId) {
        "mic_effect_golden_amber_aura" -> R.raw.mic_effect_golden_amber_aura
        "mic_effect_solar_particle_ring" -> R.raw.mic_effect_solar_particle_ring
        "mic_effect_ruby_energy_flame" -> R.raw.mic_effect_ruby_energy_flame
        "mic_effect_cyber_neon_electric" -> R.raw.mic_effect_cyber_neon_electric
        "mic_effect_diamond_crystal_vortex" -> R.raw.mic_effect_diamond_crystal_vortex
        "mic_effect_sovereign_gold_wings" -> R.raw.mic_effect_sovereign_gold_wings
        "mic_effect_emerald_celestial_wave" -> R.raw.mic_effect_emerald_celestial_wave
        else -> 0
    }
}

fun getMicEffectThumbRes(effectId: String): Int {
    return when (effectId) {
        "mic_effect_golden_amber_aura" -> R.drawable.thumb_mic_effect_golden_amber_aura
        "mic_effect_solar_particle_ring" -> R.drawable.thumb_mic_effect_solar_particle_ring
        "mic_effect_ruby_energy_flame" -> R.drawable.thumb_mic_effect_ruby_energy_flame
        "mic_effect_cyber_neon_electric" -> R.drawable.thumb_mic_effect_cyber_neon_electric
        "mic_effect_diamond_crystal_vortex" -> R.drawable.thumb_mic_effect_diamond_crystal_vortex
        "mic_effect_sovereign_gold_wings" -> R.drawable.thumb_mic_effect_sovereign_gold_wings
        "mic_effect_emerald_celestial_wave" -> R.drawable.thumb_mic_effect_emerald_celestial_wave
        else -> R.drawable.thumb_mic_effect_golden_amber_aura
    }
}

@Composable
fun VideoMicMagicEffectOverlay(
    videoRes: Int,
    avatarSize: Dp,
    effectSize: Dp,
    modifier: Modifier = Modifier
) {
    if (videoRes == 0) return

    Box(
        modifier = modifier
            .size(effectSize)
            .clip(CircleShape), // 1. COMPLETELY REMOVES 4 SQUARE CORNERS
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false // Enables transparency
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        var mediaPlayer: MediaPlayer? = null
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            val s = Surface(surface)
                            mediaPlayer = MediaPlayer().apply {
                                setSurface(s)
                                val videoUri = Uri.parse("android.resource://${ctx.packageName}/$videoRes")
                                setDataSource(ctx, videoUri)
                                isLooping = true
                                setVolume(0f, 0f)
                                setOnPreparedListener { mp ->
                                    mp.start()
                                }
                                prepareAsync()
                            }
                        }
                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            mediaPlayer?.release()
                            mediaPlayer = null
                            return true
                        }
                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    compositingStrategy = CompositingStrategy.Offscreen,
                    blendMode = BlendMode.Screen // 2. REMOVES BLACK BACKGROUND (#000000 -> 100% TRANSPARENT)
                )
                .drawWithContent {
                    drawContent()
                    // 3. PUNCH OUT 100% TRANSPARENT CIRCULAR HOLE FOR AVATAR FACE
                    drawCircle(
                        color = Color.Transparent,
                        radius = (avatarSize.toPx() / 2f),
                        blendMode = BlendMode.Clear
                    )
                }
        )
    }
}
