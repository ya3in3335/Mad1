package com.yourtech.systeme.designsystem.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.yourtech.systeme.designsystem.R
import com.yourtech.systeme.designsystem.component.BrandLogo
import com.yourtech.systeme.designsystem.theme.Tajawal
import com.yourtech.systeme.designsystem.theme.YT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TOTAL = 3.6f
private val EaseOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val EaseInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/**
 * Animated intro built from the official logo layers: the circuit "Y" is drawn upward behind a
 * scan line, its nodes light up one after another, the bar extends, the wordmark is revealed,
 * then "Smart Security Solutions". Tap to skip.
 */
@Composable
fun YourTechSplash(onFinished: () -> Unit, modifier: Modifier = Modifier, badge: String? = null) {
    val finish by rememberUpdatedState(onFinished)
    var done by remember { mutableStateOf(false) }
    val clock = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    fun complete() { if (!done) { done = true; finish() } }
    LaunchedEffect(Unit) {
        launch { delay(1250); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
        clock.animateTo(TOTAL, tween((TOTAL * 1000).toInt(), easing = LinearEasing))
        complete()
    }
    val t = clock.value
    fun seg(a: Float, d: Float) = ((t - a) / d).coerceIn(0f, 1f)
    val exit = EaseInOut.transform(seg(3.2f, 0.4f))

    BoxWithConstraints(
        modifier.fillMaxSize().background(Brush.verticalGradient(listOf(YT.NavyDeep, YT.Navy, YT.NavyDeep)))
            .clickable(remember { MutableInteractionSource() }, indication = null) { complete() },
        contentAlignment = Alignment.Center,
    ) {
        val lockupW = min(maxWidth * 0.84f, 440.dp)
        val lockupH = lockupW / BrandLogo.LOCKUP_RATIO

        // Control-room backdrop: grid, glow, radar sweep
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = 1f - exit }) {
            val c = Offset(size.width / 2f, size.height * 0.45f)
            val g = seg(0f, 0.6f)
            val step = size.minDimension / 9f
            var x = c.x % step
            while (x < size.width) { drawLine(Color.White.copy(alpha = 0.04f * g), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = c.y % step
            while (y < size.height) { drawLine(Color.White.copy(alpha = 0.04f * g), Offset(0f, y), Offset(size.width, y)); y += step }
            drawCircle(Brush.radialGradient(listOf(YT.Blue.copy(alpha = 0.30f * g), Color.Transparent), center = c, radius = size.minDimension * 0.7f), radius = size.minDimension * 0.7f, center = c)
            for (i in 1..3) drawCircle(YT.Cyan.copy(alpha = 0.08f * g), radius = size.minDimension * 0.17f * i, center = c, style = Stroke(1.5f))
            rotate(t * 120f, pivot = c) {
                drawArc(
                    Brush.sweepGradient(listOf(Color.Transparent, YT.Cyan.copy(alpha = 0.18f * g)), center = c), -60f, 60f, true,
                    topLeft = Offset(c.x - size.minDimension * 0.5f, c.y - size.minDimension * 0.5f),
                    size = androidx.compose.ui.geometry.Size(size.minDimension, size.minDimension),
                )
            }
            val ping = seg(1.25f, 0.9f)
            if (ping in 0.001f..0.999f) drawCircle(YT.Cyan.copy(alpha = 0.45f * (1 - ping)), radius = size.minDimension * 0.55f * EaseOut.transform(ping), center = c, style = Stroke(3f))
        }

        Column(
            Modifier.graphicsLayer { alpha = 1f - exit; val s = 1f + exit * 0.05f; scaleX = s; scaleY = s },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The logo artwork is left-to-right; keep its geometry in every UI language.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(Modifier.size(lockupW, lockupH)) {
                    val markP = EaseInOut.transform(seg(0.15f, 0.95f))
                    Layer(R.drawable.yt_logo_mark, BrandLogo.MARK, lockupW, lockupH,
                        Modifier
                            .graphicsLayer { val s = 0.92f + 0.08f * EaseOut.transform(seg(0.15f, 1.1f)); scaleX = s; scaleY = s }
                            .drawWithContent {
                                val top = size.height * (1f - markP)
                                clipRect(top = top) { this@drawWithContent.drawContent() }
                                if (markP in 0.01f..0.99f) {
                                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, YT.Cyan.copy(alpha = 0.6f), Color.Transparent), startY = top - 24f, endY = top + 24f), topLeft = Offset(-20f, top - 24f), size = androidx.compose.ui.geometry.Size(size.width + 40f, 48f))
                                }
                                // nodes light up bottom → top
                                BrandLogo.NODES.reversed().forEachIndexed { i, (nx, ny) ->
                                    val p = seg(0.95f + i * 0.17f, 0.7f)
                                    if (p > 0f && p < 1f) {
                                        val cxy = Offset(size.width * nx, size.height * ny)
                                        drawCircle(YT.Cyan.copy(alpha = 0.55f * (1 - p)), radius = size.width * (0.05f + 0.22f * p), center = cxy)
                                        drawCircle(YT.Cyan.copy(alpha = 0.9f * (1 - p)), radius = size.width * (0.06f + 0.3f * p), center = cxy, style = Stroke(3f))
                                    }
                                }
                            },
                    )
                    val barP = EaseOut.transform(seg(1.3f, 0.6f))
                    Layer(R.drawable.yt_logo_bar, BrandLogo.BAR, lockupW, lockupH,
                        Modifier.drawWithContent { clipRect(right = size.width * barP) { this@drawWithContent.drawContent() } })
                    val wordP = EaseInOut.transform(seg(1.6f, 0.7f))
                    Layer(R.drawable.yt_logo_word_white, BrandLogo.WORD, lockupW, lockupH,
                        Modifier
                            .graphicsLayer { alpha = seg(1.6f, 0.25f); translationX = (1 - wordP) * -30f }
                            .drawWithContent { clipRect(right = size.width * wordP) { this@drawWithContent.drawContent() } })
                }
            }
            Spacer(Modifier.height(30.dp))
            val tg = seg(2.35f, 0.55f)
            Box(Modifier.width(lockupW * (0.2f + 0.6f * EaseOut.transform(tg))).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, YT.Cyan, Color.Transparent))).graphicsLayer { alpha = tg })
            Spacer(Modifier.height(12.dp))
            Text(
                "Smart Security Solutions", color = YT.Cyan, fontFamily = Tajawal, fontWeight = FontWeight.Medium,
                fontSize = 15.sp, letterSpacing = (1.5f + 2f * EaseOut.transform(tg)).sp, modifier = Modifier.graphicsLayer { alpha = tg },
            )
            if (badge != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    badge, color = YT.White, fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.graphicsLayer { alpha = seg(2.5f, 0.4f) }.clip(RoundedCornerShape(50)).background(YT.Blue).padding(horizontal = 16.dp, vertical = 5.dp),
                )
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp).width(120.dp).height(2.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.08f))) {
            Box(Modifier.fillMaxWidth(EaseInOut.transform((t / TOTAL).coerceIn(0f, 1f))).height(2.dp).background(Brush.horizontalGradient(listOf(YT.Blue, YT.Cyan))))
        }
    }
}

@Composable
private fun Layer(res: Int, box: FloatArray, w: Dp, h: Dp, modifier: Modifier) {
    Image(
        painterResource(res), null, contentScale = ContentScale.FillBounds,
        modifier = Modifier.offset(x = w * box[0], y = h * box[1]).size(w * box[2], h * box[3]).then(modifier),
    )
}
