package com.madak.spices.designsystem.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.madak.spices.designsystem.component.LogoPart
import com.madak.spices.designsystem.component.MadakLogo
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.designsystem.theme.Tajawal
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TOTAL_SECONDS = 3.75f

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/** Ease-out with overshoot ("back" easing) for a playful pop. */
private fun easeOutBack(x: Float, overshoot: Float = 1.7f): Float {
    val c3 = overshoot + 1f
    val p = x - 1f
    return 1f + c3 * p * p * p + overshoot * p * p
}

private class Particle(val angle: Float, val distance: Float, val size: Float, val color: Color, val delay: Float, val spin: Float)
private class Grain(val dx: Float, val delay: Float, val size: Float, val drift: Float)

/**
 * Motion-graphics intro for the Madak logo.
 *
 * Timeline (seconds) — the sound design in res/raw/madak_intro.wav follows the same beats:
 *  0.00  magenta glow + golden ring expand (whoosh)
 *  0.15  chef pops in with an overshoot (low boom at 0.5)
 *  0.45  burst of spice particles
 *  0.80  spice grains fall from the chef's hand into the pot (sprinkle ticks)
 *  1.00  "مذاق" wordmark is revealed right-to-left and lands at 1.45 (thump + haptic)
 *  1.75  the green leaf grows from its stem (pluck)
 *  2.05  light shimmer sweeps across the wordmark (sparkle arpeggio)
 *  2.30  MADAK SPICES tagline (warm chord)
 *  3.30  zoom-fade exit
 *
 * Tapping anywhere skips the intro.
 */
@Composable
fun MadakMotionSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    playSound: Boolean = true,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val finish by rememberUpdatedState(onFinished)
    var done by remember { mutableStateOf(false) }
    val clock = remember { Animatable(0f) }
    val sound = remember { SplashSound(context.applicationContext) }

    fun complete() {
        if (!done) { done = true; finish() }
    }

    DisposableEffect(Unit) { onDispose { sound.fadeOutAndRelease() } }

    LaunchedEffect(Unit) {
        if (playSound) sound.play()
        launch {
            delay(1450)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        clock.animateTo(TOTAL_SECONDS, tween((TOTAL_SECONDS * 1000).toInt(), easing = LinearEasing))
        complete()
    }

    val particles = remember {
        val rnd = Random(42)
        val palette = MadakColors.spicePalette + Color.White
        List(46) {
            Particle(
                angle = rnd.nextFloat() * 2f * PI.toFloat(),
                distance = 0.35f + rnd.nextFloat() * 0.65f,
                size = 2f + rnd.nextFloat() * 5f,
                color = palette[rnd.nextInt(palette.size)],
                delay = rnd.nextFloat() * 0.25f,
                spin = rnd.nextFloat() * 0.3f - 0.15f,
            )
        }
    }
    val grains = remember {
        val rnd = Random(7)
        List(22) { Grain(rnd.nextFloat() * 0.06f - 0.03f, rnd.nextFloat() * 0.75f, 1.6f + rnd.nextFloat() * 2.4f, rnd.nextFloat() * 0.05f) }
    }

    val t = clock.value
    fun seg(start: Float, duration: Float) = ((t - start) / duration).coerceIn(0f, 1f)

    val exit = EaseInOut.transform(seg(3.3f, 0.45f))
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MadakColors.Ink)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { complete() },
        contentAlignment = Alignment.Center,
    ) {
        val logoWidth = min(maxWidth * 0.62f, 300.dp)

        // ---- Background: glow, rings and spice burst -------------------------------------------
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = 1f - exit }) {
            val center = Offset(size.width / 2f, size.height * 0.44f)
            val maxR = size.minDimension * 0.75f

            val glow = seg(0f, 0.9f) * (0.85f + 0.15f * sin(t * 3f))
            drawCircle(
                Brush.radialGradient(
                    listOf(MadakColors.Magenta.copy(alpha = 0.42f * glow), MadakColors.MagentaDeep.copy(alpha = 0.12f * glow), Color.Transparent),
                    center = center, radius = maxR,
                ),
                radius = maxR, center = center,
            )

            val ring1 = seg(0.05f, 1.0f)
            if (ring1 in 0.001f..0.999f) {
                drawCircle(
                    MadakColors.Gold.copy(alpha = (1f - ring1) * 0.9f),
                    radius = maxR * EaseOutCubic.transform(ring1), center = center,
                    style = Stroke(width = with(density) { (3.dp * (1f - ring1) + 0.5.dp).toPx() }),
                )
            }
            // Shockwave when the wordmark lands.
            val ring2 = seg(1.45f, 0.9f)
            val wordCenter = Offset(size.width / 2f, size.height * 0.62f)
            if (ring2 in 0.001f..0.999f) {
                drawCircle(
                    MadakColors.Magenta.copy(alpha = (1f - ring2) * 0.7f),
                    radius = maxR * 0.8f * EaseOutCubic.transform(ring2), center = wordCenter,
                    style = Stroke(width = with(density) { 2.dp.toPx() }),
                )
            }

            particles.forEach { p ->
                val life = seg(0.45f + p.delay, 1.35f)
                if (life <= 0f || life >= 1f) return@forEach
                val travel = EaseOutCubic.transform(life) * p.distance * maxR * 0.9f
                val a = p.angle + p.spin * life * 6f
                val pos = Offset(center.x + cos(a) * travel, center.y + sin(a) * travel + life * life * 60f)
                drawCircle(p.color.copy(alpha = (1f - life) * 0.95f), radius = with(density) { (p.size * (1f - life * 0.5f)).dp.toPx() }, center = pos)
            }
        }

        // ---- Logo layers ---------------------------------------------------------------------
        Column(
            Modifier.graphicsLayer {
                alpha = 1f - exit
                val s = 1f + exit * 0.08f
                scaleX = s; scaleY = s
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Chef + falling grains
            val chefIn = seg(0.15f, 0.7f)
            Box(
                Modifier
                    .width(logoWidth)
                    .aspectRatio(LogoPart.CHEF.ratio)
                    .graphicsLayer {
                        alpha = seg(0.15f, 0.3f)
                        val s = 0.55f + 0.45f * easeOutBack(chefIn)
                        scaleX = s; scaleY = s
                        translationY = (1f - EaseOutCubic.transform(chefIn)) * 80.dp.toPx()
                        rotationZ = (1f - EaseOutCubic.transform(chefIn)) * -8f
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
            ) {
                MadakLogo(Modifier.fillMaxSize(), part = LogoPart.CHEF)
                Canvas(Modifier.fillMaxSize()) {
                    // Hand ≈ (0.10, 0.46) of the chef artwork, pot opening ≈ (0.17, 0.74).
                    grains.forEach { g ->
                        val life = seg(0.8f + g.delay, 0.55f)
                        if (life <= 0f || life >= 1f) return@forEach
                        val x = size.width * (0.10f + g.dx + g.drift * life + 0.07f * life)
                        val y = size.height * (0.47f + 0.27f * life * life)
                        drawCircle(
                            (if (g.size > 3f) MadakColors.GoldSoft else Color.White).copy(alpha = 1f - life * 0.6f),
                            radius = g.size.dp.toPx() * 0.6f, center = Offset(x, y),
                        )
                    }
                }
            }

            Spacer(Modifier.height(logoWidth * 0.035f))

            // Wordmark: right-to-left reveal, leaf grows, then a shimmer sweep.
            val reveal = EaseInOut.transform(seg(1.0f, 0.55f))
            val land = seg(1.0f, 0.45f)
            val shimmer = seg(2.05f, 0.65f)
            Box(
                Modifier
                    .width(logoWidth * (670f / 680f))
                    .aspectRatio(LogoPart.WORDMARK_TEXT.ratio)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        if (shimmer > 0f && shimmer < 1f) {
                            val bandX = -size.width * 0.4f + size.width * 1.8f * shimmer
                            drawRect(
                                Brush.linearGradient(
                                    listOf(Color.Transparent, MadakColors.GoldSoft.copy(alpha = 0.95f), Color.Transparent),
                                    start = Offset(bandX, 0f), end = Offset(bandX + size.width * 0.35f, size.height),
                                ),
                                blendMode = BlendMode.SrcAtop,
                            )
                        }
                    },
            ) {
                MadakLogo(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = seg(1.0f, 0.2f)
                            translationY = (1f - easeOutBack(land, 1.2f)) * 36.dp.toPx()
                        }
                        .drawWithContent {
                            clipRect(left = size.width * (1f - reveal)) { this@drawWithContent.drawContent() }
                        },
                    part = LogoPart.WORDMARK_TEXT,
                )
                val leaf = seg(1.75f, 0.6f)
                MadakLogo(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = seg(1.75f, 0.12f)
                            val s = easeOutBack(leaf, 2.2f).coerceAtLeast(0f)
                            scaleX = s; scaleY = s
                            rotationZ = (1f - EaseOutCubic.transform(leaf)) * -40f
                            transformOrigin = TransformOrigin(0.15f, 0.70f)
                        },
                    part = LogoPart.WORDMARK_LEAF,
                )
            }

            Spacer(Modifier.height(26.dp))

            // Tagline
            val tag = seg(2.3f, 0.6f)
            Box(
                Modifier
                    .width(logoWidth * (0.2f + 0.8f * EaseOutCubic.transform(seg(2.2f, 0.5f))))
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color.Transparent, MadakColors.Gold, Color.Transparent)),
                    )
                    .graphicsLayer { alpha = seg(2.2f, 0.3f) },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "MADAK SPICES",
                fontFamily = Tajawal,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (2f + 5f * EaseOutCubic.transform(tag)).sp,
                color = MadakColors.Gold,
                modifier = Modifier.graphicsLayer {
                    alpha = tag
                    translationY = (1f - EaseOutCubic.transform(tag)) * 12.dp.toPx()
                },
            )
            if (subtitle != null) {
                Spacer(Modifier.height(14.dp))
                val sub = seg(2.55f, 0.5f)
                Text(
                    subtitle,
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = sub
                            val s = 0.8f + 0.2f * easeOutBack(sub)
                            scaleX = s; scaleY = s
                        }
                        .clip(RoundedCornerShape(50))
                        .background(MadakColors.Magenta)
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                )
            }
        }

        // Loading hairline at the bottom
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(120.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(FastOutSlowInEasing.transform((t / TOTAL_SECONDS).coerceIn(0f, 1f)))
                    .height(2.dp)
                    .background(MadakColors.Magenta),
            )
        }
    }
}
