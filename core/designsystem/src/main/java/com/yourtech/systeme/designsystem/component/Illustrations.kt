package com.yourtech.systeme.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.yourtech.systeme.designsystem.theme.YT
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated line illustrations for security solutions and equipment.
 * Keys: camera, dome, recorder, alarm, keypad, fingerprint, card, intercom, home, barrier, tools, shield.
 */
@Composable
fun SecurityIllustration(key: String?, modifier: Modifier = Modifier, animated: Boolean = true, background: Boolean = true) {
    val t = if (animated) {
        val tr = rememberInfiniteTransition(label = "illu")
        val v by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart), label = "illuT")
        v
    } else 0.35f
    Canvas(modifier) {
        if (background) {
            drawRect(Brush.linearGradient(listOf(Color(0xFF0D2244), Color(0xFF071426)), start = Offset.Zero, end = Offset(size.width, size.height)))
            // subtle grid
            val step = size.minDimension / 8f
            var x = step
            while (x < size.width) { drawLine(Color.White.copy(alpha = 0.035f), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = step
            while (y < size.height) { drawLine(Color.White.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y)); y += step }
            drawCircle(Brush.radialGradient(listOf(YT.Blue.copy(alpha = 0.35f), Color.Transparent)), radius = size.minDimension * 0.55f)
        }
        val s = size.minDimension
        val o = Offset((size.width - s) / 2f, (size.height - s) / 2f)
        translateSquare(o, s) { drawIllustration(key ?: "shield", t) }
    }
}

private inline fun DrawScope.translateSquare(o: Offset, s: Float, block: SquareScope.() -> Unit) {
    SquareScope(this, o, s).block()
}

private class SquareScope(val ds: DrawScope, val o: Offset, val s: Float) {
    fun p(x: Float, y: Float) = Offset(o.x + x * s, o.y + y * s)
    fun sz(w: Float, h: Float) = Size(w * s, h * s)
    val line get() = s * 0.022f
    val accent = Brush.linearGradient(listOf(YT.Blue, YT.Cyan))

    fun drawIllustration(key: String, t: Float) = with(ds) {
        val st = Stroke(width = line, cap = StrokeCap.Round)
        when (key) {
            "camera" -> {
                // field of view cone
                val cone = Path().apply { moveTo(p(0.36f, 0.47f).x, p(0.36f, 0.47f).y); lineTo(p(0.05f, 0.30f + 0.04f * sin(t * 2 * PI).toFloat()).x, p(0.05f, 0.30f).y); lineTo(p(0.05f, 0.78f).x, p(0.05f, 0.78f).y); close() }
                drawPath(cone, Brush.horizontalGradient(listOf(YT.Cyan.copy(alpha = 0.0f), YT.Cyan.copy(alpha = 0.22f)), startX = p(0.05f, 0f).x, endX = p(0.36f, 0f).x))
                rotate(-12f, pivot = p(0.55f, 0.45f)) {
                    drawRoundRect(accent, topLeft = p(0.34f, 0.36f), size = sz(0.44f, 0.2f), cornerRadius = CornerRadius(s * 0.05f), style = st)
                    drawRoundRect(YT.Surface, topLeft = p(0.35f, 0.37f), size = sz(0.42f, 0.18f), cornerRadius = CornerRadius(s * 0.05f))
                    drawCircle(YT.Cyan, radius = s * 0.06f, center = p(0.38f, 0.46f), style = st)
                    drawCircle(YT.Blue, radius = s * 0.025f, center = p(0.38f, 0.46f))
                    drawCircle(if (t < 0.5f) YT.Danger else YT.Danger.copy(alpha = 0.25f), radius = s * 0.012f, center = p(0.72f, 0.42f))
                }
                drawLine(YT.TextMuted, p(0.62f, 0.55f), p(0.68f, 0.72f), strokeWidth = line, cap = StrokeCap.Round)
                drawLine(YT.TextMuted, p(0.6f, 0.74f), p(0.82f, 0.74f), strokeWidth = line * 1.6f, cap = StrokeCap.Round)
            }
            "dome" -> {
                drawLine(YT.TextMuted, p(0.22f, 0.36f), p(0.78f, 0.36f), strokeWidth = line * 1.6f, cap = StrokeCap.Round)
                drawArc(accent, 0f, 180f, false, topLeft = p(0.26f, 0.18f), size = sz(0.48f, 0.38f), style = st)
                drawArc(YT.Surface, 0f, 180f, true, topLeft = p(0.27f, 0.19f), size = sz(0.46f, 0.36f))
                val a = t * 2 * PI
                val lc = p(0.5f + 0.06f * cos(a).toFloat(), 0.44f)
                drawCircle(YT.Cyan, radius = s * 0.055f, center = lc, style = st)
                drawCircle(YT.Blue, radius = s * 0.022f, center = lc)
                for (i in 1..3) {
                    val k = (t + i / 3f) % 1f
                    drawArc(YT.Cyan.copy(alpha = 0.35f * (1 - k)), 30f, 120f, false, topLeft = p(0.5f - 0.2f - 0.25f * k, 0.36f), size = sz(0.4f + 0.5f * k, 0.3f + 0.3f * k), style = Stroke(line * 0.7f))
                }
            }
            "recorder" -> {
                for (row in 0 until 2) {
                    val y = 0.34f + row * 0.2f
                    drawRoundRect(accent, topLeft = p(0.18f, y), size = sz(0.64f, 0.15f), cornerRadius = CornerRadius(s * 0.025f), style = st)
                    for (i in 0 until 4) {
                        val on = ((t * 8).toInt() + i + row * 2) % 3 != 0
                        drawCircle(if (on) YT.Cyan else YT.Outline, radius = s * 0.012f, center = p(0.26f + i * 0.05f, y + 0.075f))
                    }
                    drawLine(YT.TextMuted, p(0.56f, y + 0.05f), p(0.76f, y + 0.05f), strokeWidth = line * 0.6f)
                    drawLine(YT.TextMuted, p(0.56f, y + 0.1f), p(0.76f, y + 0.1f), strokeWidth = line * 0.6f)
                }
            }
            "alarm" -> {
                val bell = Path().apply {
                    moveTo(p(0.5f, 0.24f).x, p(0.5f, 0.24f).y)
                    cubicTo(p(0.66f, 0.24f).x, p(0.66f, 0.24f).y, p(0.68f, 0.4f).x, p(0.68f, 0.4f).y, p(0.68f, 0.52f).x, p(0.68f, 0.52f).y)
                    lineTo(p(0.74f, 0.64f).x, p(0.74f, 0.64f).y); lineTo(p(0.26f, 0.64f).x, p(0.26f, 0.64f).y); lineTo(p(0.32f, 0.52f).x, p(0.32f, 0.52f).y)
                    cubicTo(p(0.32f, 0.4f).x, p(0.32f, 0.4f).y, p(0.34f, 0.24f).x, p(0.34f, 0.24f).y, p(0.5f, 0.24f).x, p(0.5f, 0.24f).y)
                }
                rotate(8f * sin(t * 6 * PI).toFloat(), pivot = p(0.5f, 0.22f)) {
                    drawPath(bell, YT.Surface); drawPath(bell, accent, style = st)
                    drawCircle(YT.Cyan, radius = s * 0.035f, center = p(0.5f, 0.69f))
                }
                for (i in 0 until 3) {
                    val k = (t + i / 3f) % 1f
                    val a = 0.6f * (1 - k)
                    drawArc(YT.Cyan.copy(alpha = a), -40f, 80f, false, topLeft = p(0.5f - 0.3f - 0.12f * k, 0.44f - 0.3f - 0.12f * k), size = sz(0.6f + 0.24f * k, 0.6f + 0.24f * k), style = Stroke(line * 0.8f, cap = StrokeCap.Round))
                    drawArc(YT.Cyan.copy(alpha = a), 140f, 80f, false, topLeft = p(0.5f - 0.3f - 0.12f * k, 0.44f - 0.3f - 0.12f * k), size = sz(0.6f + 0.24f * k, 0.6f + 0.24f * k), style = Stroke(line * 0.8f, cap = StrokeCap.Round))
                }
            }
            "keypad" -> {
                drawRoundRect(YT.Surface, topLeft = p(0.3f, 0.16f), size = sz(0.4f, 0.68f), cornerRadius = CornerRadius(s * 0.05f))
                drawRoundRect(accent, topLeft = p(0.3f, 0.16f), size = sz(0.4f, 0.68f), cornerRadius = CornerRadius(s * 0.05f), style = st)
                drawRoundRect(YT.Cyan.copy(alpha = 0.25f), topLeft = p(0.36f, 0.22f), size = sz(0.28f, 0.08f), cornerRadius = CornerRadius(s * 0.015f))
                val active = (t * 12).toInt() % 12
                for (i in 0 until 12) {
                    val c = p(0.4f + (i % 3) * 0.1f, 0.4f + (i / 3) * 0.1f)
                    drawCircle(if (i == active) YT.Cyan else YT.TextMuted.copy(alpha = 0.6f), radius = s * 0.022f, center = c)
                }
            }
            "fingerprint" -> {
                for (i in 0 until 6) {
                    val r = 0.06f + i * 0.045f
                    drawArc(accent, 200f - i * 4f, 140f + i * 8f, false, topLeft = p(0.5f - r, 0.52f - r), size = sz(r * 2, r * 2), style = Stroke(line * 0.8f, cap = StrokeCap.Round))
                }
                val y = 0.22f + 0.56f * t
                drawLine(Brush.horizontalGradient(listOf(Color.Transparent, YT.Cyan, Color.Transparent), startX = p(0.18f, 0f).x, endX = p(0.82f, 0f).x), p(0.18f, y), p(0.82f, y), strokeWidth = line * 1.2f)
            }
            "card" -> {
                rotate(-8f, pivot = p(0.45f, 0.55f)) {
                    drawRoundRect(YT.Surface, topLeft = p(0.16f, 0.36f), size = sz(0.5f, 0.32f), cornerRadius = CornerRadius(s * 0.04f))
                    drawRoundRect(accent, topLeft = p(0.16f, 0.36f), size = sz(0.5f, 0.32f), cornerRadius = CornerRadius(s * 0.04f), style = st)
                    drawRoundRect(YT.Warning.copy(alpha = 0.8f), topLeft = p(0.22f, 0.44f), size = sz(0.09f, 0.07f), cornerRadius = CornerRadius(s * 0.01f))
                    drawLine(YT.TextMuted, p(0.22f, 0.6f), p(0.48f, 0.6f), strokeWidth = line * 0.6f)
                }
                for (i in 0 until 3) {
                    val k = (t + i / 3f) % 1f
                    drawArc(YT.Cyan.copy(alpha = 0.8f * (1 - k)), -50f, 100f, false, topLeft = p(0.5f - 0.1f * (1 + k * 2), 0.42f - 0.1f * (1 + k * 2)), size = sz(0.2f * (1 + k * 2), 0.2f * (1 + k * 2)), style = Stroke(line, cap = StrokeCap.Round))
                }
            }
            "intercom" -> {
                drawRoundRect(YT.Surface, topLeft = p(0.33f, 0.14f), size = sz(0.34f, 0.72f), cornerRadius = CornerRadius(s * 0.05f))
                drawRoundRect(accent, topLeft = p(0.33f, 0.14f), size = sz(0.34f, 0.72f), cornerRadius = CornerRadius(s * 0.05f), style = st)
                drawCircle(YT.Cyan, radius = s * 0.04f, center = p(0.5f, 0.26f), style = st)
                drawCircle(YT.Blue, radius = s * 0.015f, center = p(0.5f, 0.26f))
                for (i in 0 until 4) drawLine(YT.TextMuted, p(0.42f, 0.4f + i * 0.04f), p(0.58f, 0.4f + i * 0.04f), strokeWidth = line * 0.6f, cap = StrokeCap.Round)
                val pulse = 0.5f + 0.5f * sin(t * 2 * PI).toFloat()
                drawCircle(YT.Cyan.copy(alpha = 0.3f + 0.5f * pulse), radius = s * 0.055f, center = p(0.5f, 0.7f))
                drawCircle(YT.White, radius = s * 0.03f, center = p(0.5f, 0.7f))
            }
            "home" -> {
                val house = Path().apply {
                    moveTo(p(0.2f, 0.5f).x, p(0.2f, 0.5f).y); lineTo(p(0.5f, 0.24f).x, p(0.5f, 0.24f).y); lineTo(p(0.8f, 0.5f).x, p(0.8f, 0.5f).y)
                    lineTo(p(0.74f, 0.5f).x, p(0.74f, 0.5f).y); lineTo(p(0.74f, 0.8f).x, p(0.74f, 0.8f).y); lineTo(p(0.26f, 0.8f).x, p(0.26f, 0.8f).y); lineTo(p(0.26f, 0.5f).x, p(0.26f, 0.5f).y); close()
                }
                drawPath(house, YT.Surface); drawPath(house, accent, style = st)
                for (i in 0 until 3) {
                    val k = ((t * 3).toInt() % 4) > i
                    val r = 0.06f + i * 0.06f
                    drawArc(if (k) YT.Cyan else YT.Outline, 225f, 90f, false, topLeft = p(0.5f - r, 0.66f - r), size = sz(r * 2, r * 2), style = Stroke(line, cap = StrokeCap.Round))
                }
                drawCircle(YT.Cyan, radius = s * 0.02f, center = p(0.5f, 0.66f))
            }
            "barrier" -> {
                drawRoundRect(accent, topLeft = p(0.16f, 0.4f), size = sz(0.1f, 0.4f), cornerRadius = CornerRadius(s * 0.02f), style = st)
                val ang = -30f * (0.5f - 0.5f * cos(t * 2 * PI).toFloat())
                rotate(ang, pivot = p(0.21f, 0.46f)) {
                    drawRoundRect(YT.White, topLeft = p(0.21f, 0.43f), size = sz(0.62f, 0.06f), cornerRadius = CornerRadius(s * 0.03f))
                    for (i in 0 until 4) drawRect(YT.Danger, topLeft = p(0.3f + i * 0.14f, 0.43f), size = sz(0.06f, 0.06f))
                }
                drawLine(YT.TextMuted, p(0.08f, 0.82f), p(0.92f, 0.82f), strokeWidth = line, cap = StrokeCap.Round)
            }
            "tools" -> {
                rotate(t * 360f, pivot = p(0.62f, 0.38f)) {
                    for (i in 0 until 8) {
                        val a = i * PI / 4
                        drawLine(YT.Cyan, p(0.62f + 0.1f * cos(a).toFloat(), 0.38f + 0.1f * sin(a).toFloat()), p(0.62f + 0.14f * cos(a).toFloat(), 0.38f + 0.14f * sin(a).toFloat()), strokeWidth = line * 1.6f, cap = StrokeCap.Round)
                    }
                    drawCircle(YT.Cyan, radius = s * 0.1f, center = p(0.62f, 0.38f), style = st)
                }
                drawCircle(YT.Navy, radius = s * 0.04f, center = p(0.62f, 0.38f))
                rotate(45f, pivot = p(0.4f, 0.6f)) {
                    drawRoundRect(accent, topLeft = p(0.37f, 0.36f), size = sz(0.06f, 0.48f), cornerRadius = CornerRadius(s * 0.03f))
                    drawCircle(accent, radius = s * 0.07f, center = p(0.4f, 0.36f), style = Stroke(line * 1.3f))
                }
            }
            "pin" -> { // location pulse (no background)
                for (i in 0 until 3) {
                    val k = (t + i / 3f) % 1f
                    drawCircle(YT.Blue.copy(alpha = 0.5f * (1 - k)), radius = s * (0.1f + 0.4f * k), center = p(0.5f, 0.5f))
                }
            }
            else -> { // shield + radar sweep
                val shield = shieldPath(sz(0.56f, 0.64f)).also { it.translate(p(0.22f, 0.18f)) }
                drawPath(shield, YT.Surface); drawPath(shield, accent, style = st)
                rotate(t * 360f, pivot = p(0.5f, 0.48f)) {
                    drawArc(Brush.sweepGradient(listOf(Color.Transparent, YT.Cyan.copy(alpha = 0.45f)), center = p(0.5f, 0.48f)), 0f, 90f, true, topLeft = p(0.34f, 0.32f), size = sz(0.32f, 0.32f))
                }
                val check = Path().apply { moveTo(p(0.4f, 0.48f).x, p(0.4f, 0.48f).y); lineTo(p(0.48f, 0.56f).x, p(0.48f, 0.56f).y); lineTo(p(0.62f, 0.4f).x, p(0.62f, 0.4f).y) }
                drawPath(check, YT.White, style = Stroke(line * 1.3f, cap = StrokeCap.Round))
            }
        }
    }
}
