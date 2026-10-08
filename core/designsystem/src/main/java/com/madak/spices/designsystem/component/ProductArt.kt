package com.madak.spices.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Offline product visual: a stylised clay bowl filled with a mound of the spice's colour.
 * Deterministic for a given [seed] so each product keeps the same look.
 */
@Composable
fun ProductArt(color: Color, seed: String, modifier: Modifier = Modifier) {
    val grains = remember(seed) {
        val rnd = Random(seed.hashCode())
        List(70) { Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()) }
    }
    val bg = remember(color) {
        Brush.radialGradient(listOf(lerp(color, Color.White, 0.78f), lerp(color, Color(0xFFFAF6F0), 0.9f)))
    }
    Canvas(modifier.background(bg)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        // soft shadow
        drawOval(Color.Black.copy(alpha = 0.10f), topLeft = Offset(w * 0.16f, h * 0.74f), size = Size(w * 0.68f, h * 0.10f))
        // mound of spice
        val mound = Path().apply {
            moveTo(w * 0.2f, h * 0.56f)
            cubicTo(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.22f, w * 0.8f, h * 0.56f)
            close()
        }
        drawPath(mound, Brush.verticalGradient(listOf(lerp(color, Color.White, 0.25f), color, lerp(color, Color.Black, 0.25f)), startY = h * 0.25f, endY = h * 0.58f))
        // grains on the mound
        grains.forEach { (a, b, c) ->
            val x = w * (0.26f + a * 0.48f)
            val top = h * (0.33f + 0.2f * ((x - cx) / (w * 0.3f)).let { it * it })
            val y = top + (h * 0.56f - top) * b
            drawCircle(lerp(color, if (c > 0.5f) Color.White else Color.Black, 0.25f + c * 0.2f), radius = w * (0.004f + c * 0.006f), center = Offset(x, y))
        }
        // bowl
        val bowl = Path().apply {
            moveTo(w * 0.14f, h * 0.55f)
            lineTo(w * 0.86f, h * 0.55f)
            cubicTo(w * 0.84f, h * 0.74f, w * 0.68f, h * 0.80f, cx, h * 0.80f)
            cubicTo(w * 0.32f, h * 0.80f, w * 0.16f, h * 0.74f, w * 0.14f, h * 0.55f)
            close()
        }
        drawPath(bowl, Brush.verticalGradient(listOf(Color(0xFF1C1816), Color(0xFF0E0C0B)), startY = h * 0.55f, endY = h * 0.8f))
        drawOval(Color(0xFF2A2522), topLeft = Offset(w * 0.14f, h * 0.53f), size = Size(w * 0.72f, h * 0.05f))
        // gold rim line
        drawArc(
            Color(0xFFC9A46A), startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.48f), size = Size(w * 0.6f, h * 0.26f), style = Stroke(width = w * 0.008f),
        )
        // small leaf accent
        val lx = w * 0.74f
        val ly = h * 0.30f
        val leaf = Path().apply {
            moveTo(lx, ly)
            quadraticTo(lx + w * 0.10f, ly - h * 0.10f, lx + w * 0.14f, ly - h * 0.02f)
            quadraticTo(lx + w * 0.06f, ly + h * 0.06f, lx, ly)
        }
        drawPath(leaf, Color(0xFF7CC242))
        // floating specks
        repeat(6) { i ->
            val ang = (i * 60f + seed.length * 13f) * (Math.PI / 180f).toFloat()
            drawCircle(color.copy(alpha = 0.5f), radius = w * 0.012f, center = Offset(cx + cos(ang) * w * 0.38f, h * 0.30f + sin(ang) * h * 0.18f))
        }
    }
}

/** Remote image when available (Coil), always backed by the offline [ProductArt]. */
@Composable
fun ProductImage(imageUrl: String?, colorArgb: Long, seed: String, modifier: Modifier = Modifier) {
    Box(modifier) {
        ProductArt(Color(colorArgb), seed, Modifier.fillMaxSize())
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}
