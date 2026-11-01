package com.devil.livetogather

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.withFrameNanos
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private enum class FxKind { SNOW, BUTTERFLY, PETAL, EMBER, STAR }

private fun fxFor(id: String): List<FxKind> = when (id) {
    "ice_butterfly" -> listOf(FxKind.SNOW, FxKind.BUTTERFLY)
    "glacial_diamond", "barfile_pahadi" -> listOf(FxKind.SNOW, FxKind.STAR)
    "blooming_flowers", "sakura_pink", "royal_rose", "enchanted_wisteria" -> listOf(FxKind.PETAL, FxKind.BUTTERFLY)
    "solar_phoenix" -> listOf(FxKind.EMBER)
    "cosmic_gravity", "heaven_angelic" -> listOf(FxKind.STAR)
    else -> listOf(FxKind.STAR)
}

private fun fxColors(id: String): List<Color> = when (id) {
    "blooming_flowers", "sakura_pink" -> listOf(Color(0xFFFFB7D5), Color(0xFFFF80AB), Color(0xFFFFFFFF))
    "royal_rose" -> listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFFFFCDD2))
    "solar_phoenix" -> listOf(Color(0xFFFFAB00), Color(0xFFFF6D00), Color(0xFFFFEA00))
    "cosmic_gravity" -> listOf(Color(0xFFB388FF), Color(0xFFFFFFFF), Color(0xFF80D8FF))
    "heaven_angelic", "solar_gold", "golden_lotus" -> listOf(Color(0xFFFFE082), Color(0xFFFFFFFF), Color(0xFFFFD54F))
    else -> listOf(Color(0xFFFFFFFF), Color(0xFFB3E5FC), Color(0xFF80D8FF))
}

private class Fx(seed: Int, val colors: List<Color>) {
    private val r = Random(seed)
    val x0 = r.nextFloat()
    val y0 = r.nextFloat()
    val speed = 0.6f + r.nextFloat()
    val size = 0.6f + r.nextFloat()
    val phase = r.nextFloat() * (2 * PI).toFloat()
    val side = if (r.nextBoolean()) 0 else 1
    val color = colors[r.nextInt(colors.size)]
}

/**
 * Edge-to-edge luxury profile frame that is genuinely animated:
 *  - only a slim top canopy + two narrow side rails are drawn (centre stays fully transparent),
 *  - the bottom of the frame is cut away and the rails fade out,
 *  - live particles (falling snow, flying butterflies, petals, embers, twinkling stars)
 *    are drawn every frame on top of the frame art.
 * It never consumes touches, and it is placed BEHIND the profile content so every button stays visible.
 */
@Composable
fun LuxuryProfileBorderOverlay(
    frameId: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = ProfileBorderRegistry.getDrawableForId(frameId)
    val image: ImageBitmap = ImageBitmap.imageResource(id = drawableRes)
    val kinds = remember(frameId) { fxFor(frameId) }
    val colors = remember(frameId) { fxColors(frameId) }
    val flakes = remember(frameId) { List(30) { Fx(it + 1, colors) } }
    val flyers = remember(frameId) { List(6) { Fx(100 + it, colors) } }
    val petals = remember(frameId) { List(18) { Fx(200 + it, colors) } }
    val sparks = remember(frameId) { List(22) { Fx(300 + it, colors) } }

    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(frameId) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { t = (it - start) / 1_000_000_000f }
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        val w = size.width
        val h = size.height
        val dp = density
        val sw = image.width
        val sh = image.height

        // ---- Frame art: slim top canopy + two narrow rails, bottom removed ----
        val topSrcH = (sh * 0.13f).roundToInt()
        val topDstH = (w * topSrcH / sw * 0.85f).roundToInt()
        val railDstW = (w * 0.085f).roundToInt()
        val railSrcW = (sw * 0.20f).roundToInt()
        val railSrcY0 = topSrcH
        val railSrcY1 = (sh * 0.80f).roundToInt()
        val endY = (h * 0.62f).roundToInt()
        val pulse = 0.93f + 0.07f * sin(t * 1.6f)

        drawImage(image, IntOffset(0, 0), IntSize(sw, topSrcH), IntOffset(0, 0),
            IntSize(w.roundToInt(), topDstH), alpha = pulse, filterQuality = FilterQuality.Medium)
        drawImage(image, IntOffset(0, railSrcY0), IntSize(railSrcW, railSrcY1 - railSrcY0),
            IntOffset(0, topDstH - 1), IntSize(railDstW, endY - topDstH), alpha = pulse,
            filterQuality = FilterQuality.Medium)
        drawImage(image, IntOffset(sw - railSrcW, railSrcY0), IntSize(railSrcW, railSrcY1 - railSrcY0),
            IntOffset(w.roundToInt() - railDstW, topDstH - 1), IntSize(railDstW, endY - topDstH), alpha = pulse,
            filterQuality = FilterQuality.Medium)

        // fade the rails out toward the cut line
        val fadeStart = (endY * 0.78f) / h
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Black,
                    fadeStart to Color.Black,
                    (endY / h) to Color.Transparent,
                    1f to Color.Transparent
                )
            ),
            size = Size(w, h),
            blendMode = BlendMode.DstIn
        )

        // ---- Live particles ----
        val bandW = w * 0.16f
        fun bandX(f: Fx, sway: Float): Float =
            (if (f.side == 0) f.x0 * bandW else w - bandW + f.x0 * bandW) + sway

        if (FxKind.SNOW in kinds) {
            flakes.forEach { f ->
                val y = ((f.y0 * endY) + t * 26f * dp * f.speed) % endY
                val x = bandX(f, sin(t * 0.8f * f.speed + f.phase) * 10f * dp)
                val a = (0.55f + 0.4f * sin(t * 2f + f.phase)).coerceIn(0.25f, 0.95f)
                drawCircle(f.color.copy(alpha = a * 0.35f), 4.2f * dp * f.size, Offset(x, y))
                drawCircle(f.color.copy(alpha = a), 1.8f * dp * f.size, Offset(x, y))
            }
        }
        if (FxKind.PETAL in kinds) {
            petals.forEach { f ->
                val y = ((f.y0 * endY) + t * 22f * dp * f.speed) % endY
                val x = bandX(f, sin(t * 0.9f * f.speed + f.phase) * 16f * dp)
                rotate(degrees = (t * 70f * f.speed + f.phase * 57f) % 360f, pivot = Offset(x, y)) {
                    drawOval(f.color.copy(alpha = 0.85f), Offset(x - 5f * dp * f.size, y - 2.6f * dp * f.size),
                        Size(10f * dp * f.size, 5.2f * dp * f.size))
                }
            }
        }
        if (FxKind.EMBER in kinds) {
            sparks.forEach { f ->
                val y = endY - (((f.y0 * endY) + t * 34f * dp * f.speed) % endY)
                val x = bandX(f, sin(t * 1.4f * f.speed + f.phase) * 8f * dp)
                val a = (y / endY).coerceIn(0.1f, 1f)
                drawCircle(f.color.copy(alpha = a * 0.35f), 4.5f * dp * f.size, Offset(x, y))
                drawCircle(f.color.copy(alpha = a), 1.7f * dp * f.size, Offset(x, y))
            }
        }
        if (FxKind.STAR in kinds) {
            sparks.forEach { f ->
                val a = abs(sin(t * 1.6f * f.speed + f.phase))
                val x = if (f.y0 < 0.25f) f.x0 * w else bandX(f, 0f)
                val y = if (f.y0 < 0.25f) f.y0 * 0.6f * endY else f.y0 * endY
                val s = (3.5f + 3.5f * a) * dp * f.size
                drawLine(f.color.copy(alpha = a), Offset(x - s, y), Offset(x + s, y), 1.4f * dp)
                drawLine(f.color.copy(alpha = a), Offset(x, y - s), Offset(x, y + s), 1.4f * dp)
            }
        }
        if (FxKind.BUTTERFLY in kinds) {
            flyers.forEach { f -> drawButterfly(f, t, w, endY, dp) }
        }
    }
}

private fun DrawScope.drawButterfly(f: Fx, t: Float, w: Float, endY: Int, dp: Float) {
    val travel = ((f.y0 + t * 0.035f * f.speed) % 1f)
    val y = endY * 0.06f + travel * endY * 0.9f
    val center = if (f.side == 0) w * 0.055f else w * 0.945f
    val x = center + sin(t * 0.9f * f.speed + f.phase) * w * 0.07f
    val flap = 0.2f + 0.8f * abs(sin(t * 9f * f.speed + f.phase))
    val s = 11f * dp * (0.8f + f.size * 0.5f)
    val c = Color(0xFF4FC3F7)
    val edge = Color(0xFFE1F5FE)
    val tilt = sin(t * 0.9f * f.speed + f.phase) * 18f
    rotate(degrees = tilt, pivot = Offset(x, y)) {
        for (dir in intArrayOf(-1, 1)) {
            val up = Offset(if (dir < 0) x - s * flap else x, y - s * 0.9f)
            val lo = Offset(if (dir < 0) x - s * 0.8f * flap else x, y - s * 0.05f)
            drawOval(c.copy(alpha = 0.85f), up, Size(s * flap, s * 0.95f))
            drawOval(c.copy(alpha = 0.7f), lo, Size(s * 0.8f * flap, s * 0.75f))
            drawOval(edge, up, Size(s * flap, s * 0.95f), style = Stroke(1f * dp))
        }
        drawLine(Color.White, Offset(x, y - s * 0.7f), Offset(x, y + s * 0.7f), 1.8f * dp)
    }
}
