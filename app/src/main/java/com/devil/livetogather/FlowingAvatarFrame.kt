package com.devil.livetogather

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
    onClick: (() -> Unit)? = null
) {
    val frameItem = AVATAR_FRAMES_CATALOG.find { it.id == frameId }
    val effectiveVip = if (vipLevel > 0) vipLevel else (frameItem?.vipLevel ?: 0)
    val isAnimated = showAnimation && (frameItem?.isAnimated ?: frameId.isNotBlank())
    val drawableRes = frameItem?.drawableRes ?: getAvatarFrameDrawable(frameId)

    // Smooth Infinite Animation Controller for Flowing Effects
    val infiniteTransition = rememberInfiniteTransition(label = "FlowingFrameTransition")

    // 1. Rotating Aura Sweep Gradient
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )

    // 2. Realistic 3D Wing Flap & Billow (Bird/Eagle Flight Kinematics)
    val wingFlapY by infiniteTransition.animateFloat(
        initialValue = if (isAnimated) -10f else 0f,
        targetValue = if (isAnimated) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WingFlapY"
    )

    val wingLift by infiniteTransition.animateFloat(
        initialValue = if (isAnimated) 0f else 0f,
        targetValue = if (isAnimated) -5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WingLift"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isAnimated) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FramePulse"
    )

    // Aura Color Palette derived from frame
    val auraColorList = frameItem?.auraColors?.map { Color(it) } ?: listOf(
        BrandGold,
        Color(0xFFFFB300),
        BrandCoral,
        BrandGold
    )

    // Calibrated Golden Ratio: Ensures the inner hole (53.52% of frame) always clears the avatar completely
    val effectiveFrameSize = if (frameSize >= avatarSize * 1.95f) frameSize else (avatarSize * 1.95f)

    Box(
        modifier = modifier
            .size(effectiveFrameSize)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Speech Pulsing Wave
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(avatarSize + 12.dp)
                    .clip(CircleShape)
                    .background(BrandGreen.copy(alpha = 0.3f))
                    .border(1.5.dp, BrandGreen, CircleShape)
            )
        }

        // Flowing Rotating Aura Ring (Subtle behind frame)
        if (isAnimated && frameId.isNotBlank()) {
            Box(
                modifier = Modifier
                    .size(avatarSize + 6.dp)
                    .graphicsLayer(rotationZ = rotateAngle)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(auraColorList),
                        shape = CircleShape
                    )
            )
        }

        // BASE LAYER: Uncropped, Clear Profile Picture
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.2f))
                .border(
                    width = 1.5.dp,
                    color = if (isSpeaking) BrandGreen else if (frameId.isNotBlank()) auraColorList.first().copy(alpha = 0.8f) else BrandBorder,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // OVERLAY LAYER: Outer Winged Frame
        if (frameId.isNotBlank()) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = frameItem?.name ?: "Avatar Frame",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        translationY = wingLift,
                        scaleX = pulseScale,
                        scaleY = pulseScale
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // VIP Level Badge (if user has VIP level and showVipBadge is true)
        if (showVipBadge && effectiveVip > 0) {
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
                        text = if (validityLabel.isNotBlank()) "✓ Applied ($validityLabel)" else "✓ Applied",
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
                    Text("Expired • Rebuy", color = Color(0xFFFF6B6B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                    Text("💎 $formattedCost (1 Mo)", color = BrandCoral, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
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
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (canAfford) BrandGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
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
                                Text(text = "🌀", fontSize = 32.sp)
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
                    text = "💎 $formattedCost",
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
