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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.yourtech.systeme.designsystem.theme.YT
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated line illustrations for security solutions and equipment, drawn in a calm
 * silver-and-blue line style. Keys: camera, dome, recorder, alarm, keypad, fingerprint, card,
 * intercom, home, barrier, tools, pin, shield (default).
 */
@Composable
fun SecurityIllustration(key: String?, modifier: Modifier = Modifier, animated: Boolean = true, background: Boolean = true) {
    val t = if (animated) {
        val tr = rememberInfiniteTransition(label = "illu")
        val v by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart), label = "illuT")
        v
    } else 0.3f
    Canvas(modifier) {
        if (background) {
            drawRect(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            drawCircle(
                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent), center = center, radius = size.minDimension * 0.5f),
                radius = size.minDimension * 0.5f,
            )
        }
        val s = size.minDimension
        val o = Offset((size.width - s) / 2f, (size.height - s) / 2f)
        Square(this, o, s).draw(key ?: "shield", t)
    }
}

private val BgTop = Color(0xFF132036)
private val BgBottom = Color(0xFF0A1322)
private val Line = Color(0xFFD3DCE9)
private val Body = Color(0xFF1B2A44)

private class Square(val ds: DrawScope, val o: Offset, val s: Float) {
    fun p(x: Float, y: Float) = Offset(o.x + x * s, o.y + y * s)
    fun sz(w: Float, h: Float) = Size(w * s, h * s)
    fun r(v: Float) = CornerRadius(v * s)
    val w = s * 0.02f
    val stroke get() = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun thin(f: Float = 0.6f) = Stroke(width = w * f, cap = StrokeCap.Round)

    /** 0 → 1 → 0 smooth wave over the cycle. */
    fun wave(t: Float, phase: Float = 0f) = 0.5f - 0.5f * cos(((t + phase) * 2 * PI)).toFloat()

    fun path(vararg pts: Pair<Float, Float>, closed: Boolean = true) = Path().apply {
        pts.forEachIndexed { i, (x, y) -> val q = p(x, y); if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) }
        if (closed) close()
    }

    fun box(x: Float, y: Float, bw: Float, bh: Float, radius: Float, fill: Color = Body) = with(ds) {
        drawRoundRect(fill, topLeft = p(x, y), size = sz(bw, bh), cornerRadius = r(radius))
        drawRoundRect(Line, topLeft = p(x, y), size = sz(bw, bh), cornerRadius = r(radius), style = stroke)
    }

    fun draw(key: String, t: Float) = with(ds) {
        when (key) {
            "camera" -> {
                // Wall plate and arm (fixed)
                box(0.76f, 0.30f, 0.07f, 0.30f, 0.02f)
                drawLine(Line, p(0.62f, 0.45f), p(0.76f, 0.45f), strokeWidth = w * 1.4f, cap = StrokeCap.Round)
                drawCircle(Line, radius = s * 0.022f, center = p(0.62f, 0.45f))
                // Camera head pans slowly around the joint
                rotate(-6f + 10f * wave(t), pivot = p(0.62f, 0.45f)) {
                    val cone = path(0.24f to 0.38f, 0.03f to 0.24f, 0.03f to 0.58f)
                    drawPath(cone, Brush.horizontalGradient(listOf(Color.Transparent, YT.Blue.copy(alpha = 0.28f)), startX = p(0.03f, 0f).x, endX = p(0.24f, 0f).x))
                    drawRoundRect(Line.copy(alpha = 0.85f), topLeft = p(0.22f, 0.25f), size = sz(0.44f, 0.04f), cornerRadius = r(0.02f))
                    box(0.24f, 0.30f, 0.40f, 0.16f, 0.05f)
                    drawCircle(Color(0xFF0B1424), radius = s * 0.05f, center = p(0.30f, 0.38f))
                    drawCircle(Line, radius = s * 0.05f, center = p(0.30f, 0.38f), style = stroke)
                    drawCircle(YT.Blue, radius = s * 0.024f, center = p(0.30f, 0.38f))
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = s * 0.008f, center = p(0.288f, 0.37f))
                    drawCircle(YT.Danger.copy(alpha = 0.35f + 0.65f * wave(t * 2)), radius = s * 0.011f, center = p(0.58f, 0.36f))
                }
            }
            "dome" -> {
                drawLine(Line.copy(alpha = 0.5f), p(0.14f, 0.34f), p(0.86f, 0.34f), strokeWidth = w, cap = StrokeCap.Round)
                box(0.27f, 0.34f, 0.46f, 0.06f, 0.02f)
                drawArc(Body, 0f, 180f, true, topLeft = p(0.30f, 0.20f), size = sz(0.40f, 0.40f))
                drawArc(Line, 0f, 180f, false, topLeft = p(0.30f, 0.20f), size = sz(0.40f, 0.40f), style = stroke)
                val lens = p(0.5f + 0.07f * sin(t * 2 * PI).toFloat(), 0.49f)
                drawCircle(Color(0xFF0B1424), radius = s * 0.045f, center = lens)
                drawCircle(Line, radius = s * 0.045f, center = lens, style = thin(0.8f))
                drawCircle(YT.Blue, radius = s * 0.02f, center = lens)
                for (i in 0 until 2) {
                    val rr = 0.30f + i * 0.09f
                    drawArc(YT.Cyan.copy(alpha = 0.22f - i * 0.08f), 50f, 80f, false, topLeft = p(0.5f - rr, 0.40f - rr), size = sz(rr * 2, rr * 2), style = thin())
                }
            }
            "recorder" -> {
                box(0.13f, 0.38f, 0.74f, 0.22f, 0.03f)
                drawLine(Line.copy(alpha = 0.6f), p(0.60f, 0.41f), p(0.60f, 0.57f), strokeWidth = w * 0.6f)
                for (i in 0 until 4) {
                    val on = ((t * 8).toInt() + i) % 4 != 0
                    val c = if (i == 0) YT.Danger.copy(alpha = 0.4f + 0.6f * wave(t * 2)) else if (on) YT.Cyan else Line.copy(alpha = 0.25f)
                    drawCircle(c, radius = s * 0.012f, center = p(0.21f + i * 0.06f, 0.49f))
                }
                for (row in 0 until 2) {
                    drawRoundRect(Line.copy(alpha = 0.18f), topLeft = p(0.64f, 0.42f + row * 0.075f), size = sz(0.19f, 0.05f), cornerRadius = r(0.01f))
                    drawRoundRect(Line, topLeft = p(0.64f, 0.42f + row * 0.075f), size = sz(0.19f, 0.05f), cornerRadius = r(0.01f), style = thin())
                }
                drawLine(Line, p(0.20f, 0.62f), p(0.26f, 0.62f), strokeWidth = w * 1.2f, cap = StrokeCap.Round)
                drawLine(Line, p(0.74f, 0.62f), p(0.80f, 0.62f), strokeWidth = w * 1.2f, cap = StrokeCap.Round)
            }
            "alarm" -> {
                val bell = Path().apply {
                    moveTo(p(0.5f, 0.24f).x, p(0.5f, 0.24f).y)
                    cubicTo(p(0.64f, 0.24f).x, p(0.64f, 0.24f).y, p(0.67f, 0.38f).x, p(0.67f, 0.38f).y, p(0.67f, 0.50f).x, p(0.67f, 0.50f).y)
                    lineTo(p(0.73f, 0.62f).x, p(0.73f, 0.62f).y); lineTo(p(0.27f, 0.62f).x, p(0.27f, 0.62f).y); lineTo(p(0.33f, 0.50f).x, p(0.33f, 0.50f).y)
                    cubicTo(p(0.33f, 0.38f).x, p(0.33f, 0.38f).y, p(0.36f, 0.24f).x, p(0.36f, 0.24f).y, p(0.5f, 0.24f).x, p(0.5f, 0.24f).y)
                    close()
                }
                rotate(5f * sin(t * 4 * PI).toFloat(), pivot = p(0.5f, 0.20f)) {
                    drawPath(bell, Body); drawPath(bell, Line, style = stroke)
                    drawCircle(Line, radius = s * 0.022f, center = p(0.5f, 0.21f), style = thin())
                    drawCircle(YT.Blue, radius = s * 0.032f, center = p(0.5f, 0.67f))
                }
                for (i in 0 until 2) {
                    val k = (t + i * 0.5f) % 1f
                    val rr = 0.28f + 0.1f * k
                    val a = YT.Cyan.copy(alpha = 0.55f * (1 - k))
                    drawArc(a, -30f, 60f, false, topLeft = p(0.5f - rr, 0.44f - rr), size = sz(rr * 2, rr * 2), style = thin(0.9f))
                    drawArc(a, 150f, 60f, false, topLeft = p(0.5f - rr, 0.44f - rr), size = sz(rr * 2, rr * 2), style = thin(0.9f))
                }
            }
            "keypad" -> {
                box(0.30f, 0.14f, 0.40f, 0.72f, 0.05f)
                drawRoundRect(YT.Blue.copy(alpha = 0.25f), topLeft = p(0.36f, 0.21f), size = sz(0.28f, 0.09f), cornerRadius = r(0.015f))
                for (i in 0 until 4) drawCircle(Line.copy(alpha = if ((t * 5).toInt() > i) 0.9f else 0.25f), radius = s * 0.009f, center = p(0.44f + i * 0.04f, 0.255f))
                val active = (t * 6).toInt() % 12
                for (i in 0 until 12) {
                    val c = p(0.40f + (i % 3) * 0.10f, 0.40f + (i / 3) * 0.10f)
                    val on = i == active
                    drawRoundRect(if (on) YT.Blue else Line.copy(alpha = 0.12f), topLeft = Offset(c.x - s * 0.03f, c.y - s * 0.03f), size = sz(0.06f, 0.06f), cornerRadius = r(0.015f))
                    if (!on) drawRoundRect(Line.copy(alpha = 0.5f), topLeft = Offset(c.x - s * 0.03f, c.y - s * 0.03f), size = sz(0.06f, 0.06f), cornerRadius = r(0.015f), style = thin(0.5f))
                }
            }
            "fingerprint" -> {
                val scan = 0.22f + 0.56f * wave(t)
                for (i in 0 until 6) {
                    val rr = 0.07f + i * 0.042f
                    val col = if (0.52f - rr < scan) YT.Cyan else Line.copy(alpha = 0.75f)
                    drawArc(col, 200f - i * 3f, 140f + i * 6f, false, topLeft = p(0.5f - rr, 0.54f - rr), size = sz(rr * 2, rr * 2), style = thin(0.9f))
                }
                drawLine(Line.copy(alpha = 0.75f), p(0.5f, 0.54f), p(0.5f, 0.62f), strokeWidth = w * 0.9f, cap = StrokeCap.Round)
                drawLine(
                    Brush.horizontalGradient(listOf(Color.Transparent, YT.Blue, Color.Transparent), startX = p(0.18f, 0f).x, endX = p(0.82f, 0f).x),
                    p(0.18f, scan), p(0.82f, scan), strokeWidth = w,
                )
            }
            "card" -> {
                box(0.62f, 0.20f, 0.22f, 0.60f, 0.04f)
                drawCircle(YT.Success.copy(alpha = 0.4f + 0.6f * wave(t)), radius = s * 0.016f, center = p(0.73f, 0.30f))
                drawCircle(Line.copy(alpha = 0.4f), radius = s * 0.05f, center = p(0.73f, 0.52f), style = thin())
                for (i in 0 until 2) {
                    val k = (t + i * 0.5f) % 1f
                    val rr = 0.08f + 0.12f * k
                    drawArc(YT.Cyan.copy(alpha = 0.6f * (1 - k)), 145f, 70f, false, topLeft = p(0.73f - rr, 0.52f - rr), size = sz(rr * 2, rr * 2), style = thin(0.9f))
                }
                rotate(-10f, pivot = p(0.36f, 0.56f)) {
                    box(0.12f, 0.42f, 0.46f, 0.29f, 0.035f)
                    drawRoundRect(YT.Gold.copy(alpha = 0.85f), topLeft = p(0.18f, 0.49f), size = sz(0.08f, 0.065f), cornerRadius = r(0.01f))
                    drawLine(Line.copy(alpha = 0.5f), p(0.18f, 0.63f), p(0.44f, 0.63f), strokeWidth = w * 0.6f, cap = StrokeCap.Round)
                }
            }
            "intercom" -> {
                box(0.33f, 0.14f, 0.34f, 0.72f, 0.05f)
                drawCircle(Color(0xFF0B1424), radius = s * 0.04f, center = p(0.5f, 0.26f))
                drawCircle(Line, radius = s * 0.04f, center = p(0.5f, 0.26f), style = thin(0.8f))
                drawCircle(YT.Blue, radius = s * 0.016f, center = p(0.5f, 0.26f))
                for (i in 0 until 4) drawLine(Line.copy(alpha = 0.55f), p(0.43f, 0.40f + i * 0.04f), p(0.57f, 0.40f + i * 0.04f), strokeWidth = w * 0.6f, cap = StrokeCap.Round)
                val k = wave(t)
                drawCircle(YT.Blue.copy(alpha = 0.15f + 0.25f * k), radius = s * (0.055f + 0.015f * k), center = p(0.5f, 0.70f))
                drawCircle(Line, radius = s * 0.035f, center = p(0.5f, 0.70f))
            }
            "home" -> {
                val house = path(0.20f to 0.50f, 0.50f to 0.24f, 0.80f to 0.50f, 0.74f to 0.50f, 0.74f to 0.80f, 0.26f to 0.80f, 0.26f to 0.50f)
                drawPath(house, Body); drawPath(house, Line, style = stroke)
                val lit = (t * 4).toInt() % 4
                for (i in 0 until 3) {
                    val rr = 0.05f + i * 0.055f
                    drawArc(if (lit > i) YT.Cyan else Line.copy(alpha = 0.25f), 225f, 90f, false, topLeft = p(0.5f - rr, 0.70f - rr), size = sz(rr * 2, rr * 2), style = thin(0.9f))
                }
                drawCircle(YT.Cyan, radius = s * 0.018f, center = p(0.5f, 0.70f))
            }
            "barrier" -> {
                drawLine(Line.copy(alpha = 0.5f), p(0.08f, 0.82f), p(0.92f, 0.82f), strokeWidth = w, cap = StrokeCap.Round)
                // Opens, holds, closes, holds
                val phase = t * 4f
                val open = when {
                    phase < 1f -> phase
                    phase < 2f -> 1f
                    phase < 3f -> 3f - phase
                    else -> 0f
                }.let { 0.5f - 0.5f * cos(it * PI).toFloat() }
                rotate(-55f * open, pivot = p(0.21f, 0.47f)) {
                    drawRoundRect(Color(0xFFEDEFF3), topLeft = p(0.21f, 0.445f), size = sz(0.64f, 0.05f), cornerRadius = r(0.025f))
                    for (i in 0 until 4) drawRect(YT.Danger.copy(alpha = 0.85f), topLeft = p(0.31f + i * 0.13f, 0.445f), size = sz(0.055f, 0.05f))
                }
                box(0.14f, 0.42f, 0.14f, 0.40f, 0.025f)
                drawCircle(if (open > 0.5f) YT.Success else YT.Danger, radius = s * 0.016f, center = p(0.21f, 0.56f))
            }
            "tools" -> {
                rotate(t * 90f, pivot = p(0.64f, 0.36f)) {
                    for (i in 0 until 8) {
                        val a = i * PI / 4
                        drawLine(Line, p(0.64f + 0.10f * cos(a).toFloat(), 0.36f + 0.10f * sin(a).toFloat()), p(0.64f + 0.145f * cos(a).toFloat(), 0.36f + 0.145f * sin(a).toFloat()), strokeWidth = w * 2.2f, cap = StrokeCap.Round)
                    }
                    drawCircle(Body, radius = s * 0.105f, center = p(0.64f, 0.36f))
                    drawCircle(Line, radius = s * 0.105f, center = p(0.64f, 0.36f), style = stroke)
                    drawCircle(YT.Blue, radius = s * 0.035f, center = p(0.64f, 0.36f))
                }
                drawLine(Line, p(0.22f, 0.82f), p(0.44f, 0.60f), strokeWidth = w * 2.6f, cap = StrokeCap.Round)
                drawArc(Line, 100f, 290f, false, topLeft = p(0.42f, 0.52f), size = sz(0.12f, 0.12f), style = Stroke(w * 1.6f, cap = StrokeCap.Round))
            }
            "pin" -> { // location pulse (used without background)
                for (i in 0 until 2) {
                    val k = (t + i * 0.5f) % 1f
                    drawCircle(YT.Blue.copy(alpha = 0.4f * (1 - k)), radius = s * (0.1f + 0.35f * k), center = p(0.5f, 0.5f))
                }
            }
            else -> { // shield with a radar sweep kept inside the shield
                val shield = shieldPath(sz(0.54f, 0.62f)).also { it.translate(p(0.23f, 0.19f)) }
                drawPath(shield, Body)
                clipPath(shield) {
                    rotate(t * 360f, pivot = p(0.5f, 0.50f)) {
                        drawArc(
                            Brush.sweepGradient(listOf(Color.Transparent, Color.Transparent, YT.Blue.copy(alpha = 0.35f)), center = p(0.5f, 0.50f)),
                            270f, 90f, true, topLeft = p(0.15f, 0.15f), size = sz(0.7f, 0.7f),
                        )
                    }
                }
                drawPath(shield, Line, style = stroke)
                val check = path(0.40f to 0.50f, 0.48f to 0.58f, 0.62f to 0.42f, closed = false)
                drawPath(check, Color.White, style = Stroke(w * 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
