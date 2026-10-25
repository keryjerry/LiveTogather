package com.devil.livetogather

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-End Luxury 3D Laser / Metallic Energy Connector Visual.
 * Replaces cartoonish emojis with dynamic dual-node energy bridges
 * for live mic seats and intimacy store display.
 */
@Composable
fun MicLinkPreviewVisual(
    item: StoreItem,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    if (item.drawableRes != 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "MicLinkGlow")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(item.primaryColor).copy(alpha = 0.28f),
                            Color(0xFF0F0B1E).copy(alpha = 0.88f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                contentScale = ContentScale.Fit
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MicLinkAnimation")

    // Pulse wave traveling across the bridge
    val energyPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EnergyPulse"
    )

    // Breathing glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val primary = Color(item.primaryColor)
    val secondary = Color(item.secondaryColor)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        primary.copy(alpha = 0.35f),
                        Color(0xFF0F0B1E).copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cy = h / 2f

            // Left Node (Mic Seat A)
            val leftNodeX = w * 0.22f
            // Right Node (Mic Seat B)
            val rightNodeX = w * 0.78f

            val nodeRadius = 8.dp.toPx()

            // 1. Dual Outer Aura Halos
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primary.copy(alpha = glowAlpha * 0.5f), Color.Transparent),
                    center = Offset(leftNodeX, cy),
                    radius = nodeRadius * 2.2f
                ),
                center = Offset(leftNodeX, cy),
                radius = nodeRadius * 2.2f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(secondary.copy(alpha = glowAlpha * 0.5f), Color.Transparent),
                    center = Offset(rightNodeX, cy),
                    radius = nodeRadius * 2.2f
                ),
                center = Offset(rightNodeX, cy),
                radius = nodeRadius * 2.2f
            )

            // 2. Thick Luminous Laser Beam / Interlocking Chain Core
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(primary, secondary, primary),
                    startX = leftNodeX,
                    endX = rightNodeX
                ),
                start = Offset(leftNodeX, cy),
                end = Offset(rightNodeX, cy),
                strokeWidth = 4.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3. High-Intensity White-Hot Laser Center
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.White, Color.White.copy(alpha = 0.3f)),
                    startX = leftNodeX,
                    endX = rightNodeX
                ),
                start = Offset(leftNodeX, cy),
                end = Offset(rightNodeX, cy),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 4. Moving Pulse Particle along the beam
            val particleX = leftNodeX + (rightNodeX - leftNodeX) * energyPhase
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(particleX, cy)
            )

            // 5. Left Metallic Ring & Core
            drawCircle(
                brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
                radius = nodeRadius,
                center = Offset(leftNodeX, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = primary,
                radius = nodeRadius * 0.6f,
                center = Offset(leftNodeX, cy)
            )

            // 6. Right Metallic Ring & Core
            drawCircle(
                brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF2979FF))),
                radius = nodeRadius,
                center = Offset(rightNodeX, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = secondary,
                radius = nodeRadius * 0.6f,
                center = Offset(rightNodeX, cy)
            )
        }

        // Center Crest Emblem
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF130E24))
                .border(1.dp, Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    item.id.contains("royal") || item.id.contains("chain") -> "👑"
                    item.id.contains("cyber") || item.id.contains("beam") -> "⚡"
                    item.id.contains("pulse") || item.id.contains("sapphire") -> "💠"
                    item.id.contains("matrix") || item.id.contains("emerald") -> "💎"
                    else -> "✨"
                },
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Real-Time Electric Current Plasma Beam Composable.
 * Draws a crackling high-voltage lightning discharge arc between two points.
 */
@Composable
fun ElectricCurrentBeam(
    startOffset: Offset,
    endOffset: Offset,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00F5FF),
    secondaryColor: Color = Color(0xFF9D4EDD)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ElectricBeamTransition")
    val sparkStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SparkStep"
    )

    Canvas(modifier = modifier) {
        val dx = endOffset.x - startOffset.x
        val dy = endOffset.y - startOffset.y
        val dist = hypot(dx, dy)
        if (dist <= 1f) return@Canvas

        val nx = -dy / dist
        val ny = dx / dist

        // Helper to draw a crackling fractal bolt
        fun drawBolt(seed: Int, maxDisplace: Float, color: Color, widthPx: Float) {
            val rng = Random(seed + sparkStep.toInt() * 7)
            val path = Path()
            path.moveTo(startOffset.x, startOffset.y)

            val segments = 8
            for (i in 1 until segments) {
                val t = i.toFloat() / segments
                val midX = startOffset.x + dx * t
                val midY = startOffset.y + dy * t
                val offset = (rng.nextFloat() - 0.5f) * maxDisplace * 2f
                path.lineTo(midX + nx * offset, midY + ny * offset)
            }
            path.lineTo(endOffset.x, endOffset.y)

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = widthPx, cap = StrokeCap.Round)
            )
        }

        // Layer 1: Wide Electric Cyan Glow
        drawBolt(seed = 101, maxDisplace = 14.dp.toPx(), color = primaryColor.copy(alpha = 0.25f), widthPx = 8.dp.toPx())

        // Layer 2: Secondary Violet Crackle Branch
        drawBolt(seed = 202, maxDisplace = 18.dp.toPx(), color = secondaryColor.copy(alpha = 0.45f), widthPx = 4.dp.toPx())

        // Layer 3: Main High-Voltage Plasma Arc
        drawBolt(seed = 303, maxDisplace = 10.dp.toPx(), color = primaryColor, widthPx = 2.5.dp.toPx())

        // Layer 4: White-Hot Center Spark
        drawBolt(seed = 303, maxDisplace = 10.dp.toPx(), color = Color.White, widthPx = 1.2.dp.toPx())
    }
}

/**
 * Live Room Stage Mic Link Connector Overlay.
 * Connects two live mic seats with genuine high-voltage fractal lightning plasma
 * and centers the photorealistic 3D gemstone / crown emblem between them.
 */
@Composable
fun StageMicLinkOverlay(
    startOffset: Offset,
    endOffset: Offset,
    drawableRes: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFF1744),
    secondaryColor: Color = Color(0xFFFFD700),
    emblemSize: Dp = 48.dp
) {
    Box(modifier = modifier) {
        // 1. High-Voltage Fractal Electric Plasma Beam
        ElectricCurrentBeam(
            startOffset = startOffset,
            endOffset = endOffset,
            modifier = Modifier.fillMaxSize(),
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )

        // 2. Centered Photorealistic 3D Gemstone Emblem
        val midX = (startOffset.x + endOffset.x) / 2f
        val midY = (startOffset.y + endOffset.y) / 2f

        if (drawableRes != 0) {
            val infiniteTransition = rememberInfiniteTransition(label = "EmblemShockwave")
            val shockwaveScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "shockwave"
            )

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (midX - (emblemSize.toPx() / 2)).toInt(),
                            (midY - (emblemSize.toPx() / 2)).toInt()
                        )
                    }
                    .size(emblemSize),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow
                Box(
                    modifier = Modifier
                        .size(emblemSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "Active Mic Link",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = shockwaveScale, scaleY = shockwaveScale),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
