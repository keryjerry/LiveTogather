package com.devil.livetogather

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 360° Continuous Rotating Cosmic Galaxy Vortex Mic Effect.
 * Specifically crafted for clean circular profile pictures WITHOUT VIP frames.
 * Hugs avatar perimeter snugly, never collides with adjacent mic seats on stage.
 */
@Composable
fun CosmicGalaxyVortexEffect(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 38.dp,
    size: Dp = avatarSize + 18.dp,
    isSpeaking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CosmicGalaxy360")

    // 1. Smooth 360° continuous rotation (4.5s period)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CosmicRotation"
    )

    // 2. Counter-rotation for secondary laser ring (3.5s period)
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CosmicCounterRotation"
    )

    // 3. Speaking / breathing pulse wave
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSpeaking) 3.5f else 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 350 else 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CosmicPulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val baseRadius = (avatarSize / 2f).toPx()
        val pulsePx = pulse.dp.toPx()

        // 1. Soft Nebular Aura right at the outer perimeter
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x339D4EDD), // Electric violet
                    Color(0x2200F5FF), // Cyan
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = baseRadius + 10.dp.toPx() + pulsePx
            ),
            radius = baseRadius + 10.dp.toPx() + pulsePx,
            center = Offset(cx, cy)
        )

        // 2. 4 Spiraling Galaxy Filaments hugging the circle tightly (360° rotation)
        rotate(degrees = rotationAngle, pivot = Offset(cx, cy)) {
            val numFilaments = 4
            for (f in 0 until numFilaments) {
                val baseAngle = f * (2f * Math.PI.toFloat() / numFilaments)
                val path = Path()
                var first = true
                for (s in 0..16) {
                    val r = baseRadius + 1.5.dp.toPx() + (s * 0.45.dp.toPx()) + (pulsePx * 0.25f)
                    val a = baseAngle + (s * 0.08f)
                    val px = cx + cos(a) * r
                    val py = cy + sin(a) * r
                    if (first) {
                        path.moveTo(px, py)
                        first = false
                    } else {
                        path.lineTo(px, py)
                    }
                }
                val filamentColor = when (f % 4) {
                    0 -> Color(0xFF00F5FF) // Electric Cyan
                    1 -> Color(0xFFFF007F) // Vivid Magenta
                    2 -> Color(0xFF9D4EDD) // Ultraviolet
                    else -> Color(0xFFFFD700) // Starlight Gold
                }
                drawPath(
                    path = path,
                    color = filamentColor.copy(alpha = if (isSpeaking) 0.95f else 0.82f),
                    style = Stroke(width = if (isSpeaking) 2.2f else 1.8f, cap = StrokeCap.Round)
                )
            }
        }

        // 3. Counter-rotating outer laser ring dashes
        rotate(degrees = counterRotation, pivot = Offset(cx, cy)) {
            val outerR = baseRadius + 5.5.dp.toPx() + (pulsePx * 0.4f)
            val dashCount = 8
            for (d in 0 until dashCount) {
                val startA = d * (360f / dashCount)
                if (d % 2 == 0) {
                    drawArc(
                        color = Color(0xFFFF4081).copy(alpha = if (isSpeaking) 0.9f else 0.75f),
                        startAngle = startA,
                        sweepAngle = 18f,
                        useCenter = false,
                        topLeft = Offset(cx - outerR, cy - outerR),
                        size = Size(outerR * 2, outerR * 2),
                        style = Stroke(width = 1.4f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Inner complementary laser ring
        rotate(degrees = rotationAngle * 1.3f, pivot = Offset(cx, cy)) {
            val innerR = baseRadius + 2.5.dp.toPx() + (pulsePx * 0.2f)
            val dashCount = 6
            for (d in 0 until dashCount) {
                val startA = d * (360f / dashCount)
                if (d % 2 == 1) {
                    drawArc(
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                        startAngle = startA,
                        sweepAngle = 22f,
                        useCenter = false,
                        topLeft = Offset(cx - innerR, cy - innerR),
                        size = Size(innerR * 2, innerR * 2),
                        style = Stroke(width = 1.2f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 4. 4 Revolving Micro-Pulsar Stars with 4-Point Flares
        rotate(degrees = -rotationAngle * 1.5f, pivot = Offset(cx, cy)) {
            val starR = baseRadius + 6.dp.toPx() + pulsePx
            for (s in 0 until 4) {
                val starA = s * (Math.PI.toFloat() / 2f)
                val sx = cx + cos(starA) * starR
                val sy = cy + sin(starA) * starR

                // Sparkle cross flare
                val fLen = 4.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(sx - fLen, sy),
                    end = Offset(sx + fLen, sy),
                    strokeWidth = 1.3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(sx, sy - fLen),
                    end = Offset(sx, sy + fLen),
                    strokeWidth = 1.3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color(0xFF00FFFF),
                    radius = 1.8f,
                    center = Offset(sx, sy)
                )
            }
        }

        // 5. Orbiting Starlight Nano-Particles (12 fixed orbiting angles)
        val particleAngles = listOf(
            0.15f, 0.72f, 1.25f, 1.88f, 2.45f, 3.10f,
            3.65f, 4.22f, 4.81f, 5.35f, 5.89f, 6.18f
        )
        for (i in particleAngles.indices) {
            val baseAngle = particleAngles[i]
            val speedFactor = if (i % 2 == 0) 1.1f else 0.85f
            val curAngle = (baseAngle + (rotationAngle * Math.PI.toFloat() / 180f) * speedFactor) % (2f * Math.PI.toFloat())
            val pRadius = baseRadius + 3.dp.toPx() + ((i % 4) * 1.2.dp.toPx()) + (pulsePx * 0.3f)
            val px = cx + cos(curAngle) * pRadius
            val py = cy + sin(curAngle) * pRadius
            val pColor = when (i % 3) {
                0 -> Color(0xFF00F5FF)
                1 -> Color(0xFFFF69B4)
                else -> Color.White
            }
            drawCircle(
                color = pColor.copy(alpha = if (isSpeaking) 0.9f else 0.7f),
                radius = if (i % 3 == 0) 1.5f else 1.0f,
                center = Offset(px, py)
            )
        }
    }
}
