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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import com.yourtech.systeme.designsystem.theme.YT
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Solid pictogram illustrations (one colour, bold silhouettes, details cut out as negative space),
 * in the style of the classic CCTV sign. Shown dark-on-light on their own tile, or white when drawn
 * without a tile. Motion is minimal. Keys: camera, dome, recorder, alarm, keypad, fingerprint, card,
 * intercom, home, barrier, tools, pin, shield (default).
 */
@Composable
fun SecurityIllustration(key: String?, modifier: Modifier = Modifier, animated: Boolean = true, background: Boolean = true, tint: Color? = null) {
    val t = if (animated) {
        val tr = rememberInfiniteTransition(label = "illu")
        val v by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart), label = "illuT")
        v
    } else 0f
    val ink = tint ?: if (background) Ink else Color.White
    Canvas(
        modifier
            .drawBehind { if (background) drawRect(Tile) }
            // Offscreen layer so cut-outs (BlendMode.Clear) reveal whatever is behind the glyph.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val s = size.minDimension * 0.86f
        val o = Offset((size.width - s) / 2f, (size.height - s) / 2f)
        Glyph(this, o, s, ink).draw(aliases[key] ?: key ?: "shield", t)
    }
}

private val aliases = mapOf("doorbell" to "intercom", "gate" to "barrier", "build" to "tools", "lock" to "keypad", "access" to "keypad", "smart" to "home", "wifi" to "home")

private val Tile = Color(0xFFF3F5F9)
private val Ink = Color(0xFF0B1322)

private class Glyph(val ds: DrawScope, val o: Offset, val s: Float, val ink: Color) {
    fun p(x: Float, y: Float) = Offset(o.x + x * s, o.y + y * s)

    /** 0 → 1 → 0 over the cycle. */
    fun wave(t: Float) = 0.5f - 0.5f * cos(t * 2 * PI).toFloat()

    fun poly(vararg pts: Pair<Float, Float>) = Path().apply {
        pts.forEachIndexed { i, (x, y) -> val q = p(x, y); if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) }
        close()
    }

    fun rrect(x: Float, y: Float, w: Float, h: Float, r: Float) =
        Path().apply { addRoundRect(RoundRect(Rect(p(x, y), p(x + w, y + h)), CornerRadius(r * s))) }

    fun fill(path: Path) = ds.drawPath(path, ink)
    fun cut(path: Path) = ds.drawPath(path, Color.Black, blendMode = BlendMode.Clear)
    fun dot(x: Float, y: Float, r: Float, color: Color = ink) = ds.drawCircle(color, r * s, p(x, y))
    fun cutDot(x: Float, y: Float, r: Float) = ds.drawCircle(Color.Black, r * s, p(x, y), blendMode = BlendMode.Clear)
    fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float, width: Float, color: Color = ink) =
        ds.drawArc(color, start, sweep, false, topLeft = p(cx - r, cy - r), size = androidx.compose.ui.geometry.Size(2 * r * s, 2 * r * s), style = Stroke(width * s, cap = StrokeCap.Round))

    /** Maps the reference CCTV pictogram (1080×656 artwork) into the unit square. */
    fun c(x: Float, y: Float) = ((x - 268f) / 545f) to (0.12f + (y - 170f) / 545f)

    fun draw(key: String, t: Float) = with(ds) {
        when (key) {
            "camera" -> {
                // Wall bracket stays still; the camera pans gently around its mount.
                fill(Path().apply {
                    val a = c(778f, 445f); val b = c(806f, 592f)
                    addRoundRect(RoundRect(Rect(p(a.first, a.second), p(b.first, b.second)), topLeft = CornerRadius(0.05f * s), bottomLeft = CornerRadius(0.05f * s)))
                })
                val pivot = c(778f, 520f)
                rotate(-3f + 6f * wave(t), pivot = p(pivot.first, pivot.second)) {
                    fill(poly(c(276f, 333f), c(745f, 176f), c(764f, 191f), c(803f, 312f), c(793f, 330f), c(460f, 438f)))
                    cut(poly(c(588f, 254f), c(723f, 209f), c(731f, 226f), c(595f, 272f)))
                    fill(poly(c(282f, 365f), c(432f, 448f), c(398f, 460f), c(293f, 402f)))
                    fill(poly(c(545f, 412f), c(590f, 397f), c(693f, 498f), c(780f, 498f), c(780f, 543f), c(676f, 543f)))
                }
            }
            "dome" -> {
                fill(rrect(0.14f, 0.24f, 0.72f, 0.08f, 0.02f))
                val dome = Path().apply { arcTo(Rect(p(0.24f, 0.10f), p(0.76f, 0.62f)), 0f, 180f, true); close() }
                fill(dome)
                val lx = 0.5f + 0.08f * sin(t * 2 * PI).toFloat()
                cutDot(lx, 0.47f, 0.085f)
                dot(lx, 0.47f, 0.045f)
                cutDot(lx - 0.015f, 0.455f, 0.012f)
            }
            "recorder" -> {
                fill(rrect(0.08f, 0.34f, 0.84f, 0.28f, 0.04f))
                fill(rrect(0.16f, 0.64f, 0.10f, 0.05f, 0.015f)); fill(rrect(0.74f, 0.64f, 0.10f, 0.05f, 0.015f))
                for (i in 0 until 3) cutDot(0.19f + i * 0.08f, 0.48f, 0.025f)
                if (wave(t * 2) > 0.5f) dot(0.19f, 0.48f, 0.012f) // activity light blinks
                cut(rrect(0.50f, 0.40f, 0.34f, 0.055f, 0.02f))
                cut(rrect(0.50f, 0.505f, 0.34f, 0.055f, 0.02f))
            }
            "alarm" -> {
                val swing = 4f * sin(t * 4 * PI).toFloat()
                rotate(swing, pivot = p(0.5f, 0.18f)) {
                    val bell = Path().apply {
                        moveTo(p(0.5f, 0.18f).x, p(0.5f, 0.18f).y)
                        cubicTo(p(0.68f, 0.18f).x, p(0.68f, 0.18f).y, p(0.70f, 0.36f).x, p(0.70f, 0.36f).y, p(0.70f, 0.50f).x, p(0.70f, 0.50f).y)
                        lineTo(p(0.78f, 0.64f).x, p(0.78f, 0.64f).y); lineTo(p(0.22f, 0.64f).x, p(0.22f, 0.64f).y); lineTo(p(0.30f, 0.50f).x, p(0.30f, 0.50f).y)
                        cubicTo(p(0.30f, 0.36f).x, p(0.30f, 0.36f).y, p(0.32f, 0.18f).x, p(0.32f, 0.18f).y, p(0.5f, 0.18f).x, p(0.5f, 0.18f).y)
                        close()
                    }
                    fill(bell)
                    cut(rrect(0.36f, 0.30f, 0.05f, 0.18f, 0.025f))
                    dot(0.5f, 0.72f, 0.055f)
                }
                val a = 0.35f + 0.65f * wave(t * 2)
                arc(0.5f, 0.42f, 0.36f, -35f, 50f, 0.045f, ink.copy(alpha = a))
                arc(0.5f, 0.42f, 0.36f, 165f, 50f, 0.045f, ink.copy(alpha = a))
            }
            "keypad" -> {
                fill(rrect(0.27f, 0.08f, 0.46f, 0.84f, 0.07f))
                cut(rrect(0.34f, 0.16f, 0.32f, 0.11f, 0.02f))
                val active = (t * 12).toInt() % 12
                for (i in 0 until 12) {
                    val x = 0.35f + (i % 3) * 0.105f
                    val y = 0.34f + (i / 3) * 0.13f
                    val key = rrect(x, y, 0.08f, 0.09f, 0.018f)
                    cut(key)
                    if (i == active) fill(rrect(x + 0.02f, y + 0.022f, 0.04f, 0.046f, 0.01f))
                }
            }
            "fingerprint" -> {
                for (i in 0 until 5) {
                    val r = 0.08f + i * 0.075f
                    arc(0.5f, 0.56f, r, 195f - i * 4f, 150f + i * 8f, 0.045f)
                }
                fill(rrect(0.477f, 0.56f, 0.046f, 0.16f, 0.023f))
                val y = 0.18f + 0.62f * wave(t)
                cut(rrect(0.08f, y - 0.012f, 0.84f, 0.024f, 0.012f))
            }
            "card" -> {
                fill(rrect(0.60f, 0.12f, 0.28f, 0.76f, 0.05f))
                cutDot(0.74f, 0.24f, 0.03f)
                if (wave(t * 2) > 0.5f) dot(0.74f, 0.24f, 0.016f)
                // Contactless symbol cut into the reader face.
                cutDot(0.67f, 0.56f, 0.022f)
                for (i in 0 until 2) {
                    val r = 0.06f + i * 0.055f
                    val wavePath = Path().apply { arcTo(Rect(p(0.67f - r, 0.56f - r), p(0.67f + r, 0.56f + r)), -50f, 100f, true) }
                    ds.drawPath(wavePath, Color.Black, style = Stroke(0.03f * s, cap = StrokeCap.Round), blendMode = BlendMode.Clear)
                }
                rotate(-12f, pivot = p(0.34f, 0.58f)) {
                    cut(rrect(0.07f, 0.40f, 0.56f, 0.38f, 0.05f))
                    fill(rrect(0.10f, 0.43f, 0.50f, 0.32f, 0.04f))
                    cut(rrect(0.16f, 0.50f, 0.10f, 0.08f, 0.015f))
                    cut(rrect(0.16f, 0.64f, 0.34f, 0.035f, 0.017f))
                }
            }
            "intercom" -> {
                fill(rrect(0.29f, 0.06f, 0.42f, 0.88f, 0.07f))
                cutDot(0.5f, 0.21f, 0.075f)
                dot(0.5f, 0.21f, 0.035f)
                for (i in 0 until 4) cut(rrect(0.38f, 0.37f + i * 0.055f, 0.24f, 0.025f, 0.012f))
                cutDot(0.5f, 0.75f, 0.08f)
                dot(0.5f, 0.75f, 0.045f + 0.012f * wave(t))
            }
            "home" -> {
                fill(poly(0.10f to 0.50f, 0.50f to 0.14f, 0.90f to 0.50f, 0.80f to 0.50f, 0.80f to 0.88f, 0.20f to 0.88f, 0.20f to 0.50f))
                fill(rrect(0.66f, 0.18f, 0.08f, 0.18f, 0.01f))
                val lit = (t * 4).toInt() % 4
                for (i in 0 until 3) {
                    val r = 0.06f + i * 0.075f
                    val path = Path().apply { arcTo(Rect(p(0.5f - r, 0.78f - r), p(0.5f + r, 0.78f + r)), 225f, 90f, false) }
                    if (i <= lit) ds.drawPath(path, Color.Black, style = Stroke(0.035f * s, cap = StrokeCap.Round), blendMode = BlendMode.Clear)
                }
                cutDot(0.5f, 0.78f, 0.03f)
            }
            "barrier" -> {
                val phase = t * 4f
                val open = when {
                    phase < 1f -> phase
                    phase < 2f -> 1f
                    phase < 3f -> 3f - phase
                    else -> 0f
                }.let { 0.5f - 0.5f * cos(it * PI).toFloat() }
                rotate(-60f * open, pivot = p(0.21f, 0.46f)) {
                    fill(rrect(0.21f, 0.42f, 0.71f, 0.08f, 0.04f))
                    for (i in 0 until 4) cut(poly((0.34f + i * 0.14f) to 0.42f, (0.40f + i * 0.14f) to 0.42f, (0.36f + i * 0.14f) to 0.50f, (0.30f + i * 0.14f) to 0.50f))
                }
                fill(rrect(0.12f, 0.36f, 0.18f, 0.52f, 0.03f))
                cutDot(0.21f, 0.46f, 0.03f)
                fill(rrect(0.06f, 0.86f, 0.88f, 0.05f, 0.025f))
            }
            "tools" -> {
                rotate(t * 90f, pivot = p(0.66f, 0.32f)) {
                    for (i in 0 until 8) {
                        rotate(i * 45f, pivot = p(0.66f, 0.32f)) { fill(rrect(0.66f + 0.12f, 0.32f - 0.035f, 0.08f, 0.07f, 0.015f)) }
                    }
                    fill(Path().apply { addOval(Rect(p(0.66f - 0.15f, 0.32f - 0.15f), p(0.66f + 0.15f, 0.32f + 0.15f))) })
                    cutDot(0.66f, 0.32f, 0.06f)
                }
                rotate(45f, pivot = p(0.30f, 0.70f)) {
                    fill(rrect(0.25f, 0.56f, 0.10f, 0.40f, 0.05f))
                    fill(Path().apply { addOval(Rect(p(0.18f, 0.40f), p(0.42f, 0.64f))) })
                    cut(rrect(0.26f, 0.38f, 0.08f, 0.13f, 0.02f))
                }
            }
            "cable" -> {
                fill(rrect(0.37f, 0.08f, 0.07f, 0.17f, 0.02f)); fill(rrect(0.56f, 0.08f, 0.07f, 0.17f, 0.02f))
                fill(rrect(0.28f, 0.23f, 0.44f, 0.28f, 0.07f))
                cut(rrect(0.36f, 0.33f, 0.28f, 0.035f, 0.017f))
                fill(rrect(0.44f, 0.50f, 0.12f, 0.10f, 0.02f))
                val cord = Path().apply {
                    moveTo(p(0.5f, 0.58f).x, p(0.5f, 0.58f).y)
                    cubicTo(p(0.5f, 0.80f).x, p(0.5f, 0.80f).y, p(0.22f, 0.70f).x, p(0.22f, 0.70f).y, p(0.16f, 0.90f).x, p(0.16f, 0.90f).y)
                }
                ds.drawPath(cord, ink, style = Stroke(0.06f * s, cap = StrokeCap.Round))
            }
            "pin" -> {
                val k = t
                ds.drawCircle(ink.copy(alpha = 0.35f * (1 - k)), s * (0.12f + 0.3f * k), p(0.5f, 0.84f), style = Stroke(0.02f * s))
                val pin = Path().apply {
                    moveTo(p(0.5f, 0.86f).x, p(0.5f, 0.86f).y)
                    cubicTo(p(0.36f, 0.66f).x, p(0.36f, 0.66f).y, p(0.24f, 0.52f).x, p(0.24f, 0.52f).y, p(0.24f, 0.38f).x, p(0.24f, 0.38f).y)
                    cubicTo(p(0.24f, 0.22f).x, p(0.24f, 0.22f).y, p(0.36f, 0.10f).x, p(0.36f, 0.10f).y, p(0.5f, 0.10f).x, p(0.5f, 0.10f).y)
                    cubicTo(p(0.64f, 0.10f).x, p(0.64f, 0.10f).y, p(0.76f, 0.22f).x, p(0.76f, 0.22f).y, p(0.76f, 0.38f).x, p(0.76f, 0.38f).y)
                    cubicTo(p(0.76f, 0.52f).x, p(0.76f, 0.52f).y, p(0.64f, 0.66f).x, p(0.64f, 0.66f).y, p(0.5f, 0.86f).x, p(0.5f, 0.86f).y)
                    close()
                }
                fill(pin)
                cutDot(0.5f, 0.38f, 0.10f)
            }
            else -> { // shield with a cut-out check mark
                val shield = shieldPath(androidx.compose.ui.geometry.Size(0.76f * s, 0.86f * s)).also { it.translate(p(0.12f, 0.07f)) }
                fill(shield)
                val check = Path().apply {
                    moveTo(p(0.34f, 0.50f).x, p(0.34f, 0.50f).y); lineTo(p(0.46f, 0.62f).x, p(0.46f, 0.62f).y); lineTo(p(0.68f, 0.38f).x, p(0.68f, 0.38f).y)
                }
                ds.drawPath(check, Color.Black, style = Stroke(0.075f * s, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round), blendMode = BlendMode.Clear)
                // A soft light sweep across the shield, very subtle.
                val x = -0.2f + 1.4f * t
                ds.drawPath(shield, androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = if (ink == Color.White) 0f else 0.18f), Color.Transparent),
                    start = p(x - 0.15f, 0f), end = p(x + 0.15f, 0.3f),
                ), blendMode = BlendMode.SrcAtop)
            }
        }
    }
}
