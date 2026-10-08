package com.yourtech.systeme.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yourtech.systeme.designsystem.R

/**
 * Official YOURTECH SYSTEME logo (docs/brand/yourtech-logo-original.png), split into layers:
 * the circuit "Y" mark, the top bar and the wordmark. On dark surfaces the wordmark is shown in
 * white (light-on-dark variant); shapes and proportions are untouched.
 */
object BrandLogo {
    /** Full lockup is 3321 × 1192 px; layer boxes below are fractions of it. */
    const val LOCKUP_RATIO = 3321f / 1192f
    const val MARK_RATIO = 779f / 1192f
    val MARK = floatArrayOf(0f, 0f, 779f / 3321f, 1f)
    val BAR = floatArrayOf(813f / 3321f, 0f, 2508f / 3321f, 100f / 1192f)
    val WORD = floatArrayOf(845f / 3321f, 271f / 1192f, 2233f / 3321f, 658f / 1192f)

    /** Circuit nodes inside the mark (x, y fractions), from top to bottom. */
    val NODES = listOf(0.253f to 0.152f, 0.449f to 0.463f, 0.515f to 0.681f, 0.271f to 0.911f)
}

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.yt_logo_mark), "YOURTECH SYSTEME", modifier.aspectRatio(BrandLogo.MARK_RATIO), contentScale = ContentScale.Fit)
}

/** Full logo; [onDark] uses the white-lettering variant. */
@Composable
fun BrandLockup(modifier: Modifier = Modifier, onDark: Boolean = true) {
    Image(
        painterResource(if (onDark) R.drawable.yt_logo_full_white else R.drawable.yt_logo_full),
        "YOURTECH SYSTEME",
        modifier.aspectRatio(BrandLogo.LOCKUP_RATIO),
        contentScale = ContentScale.Fit,
    )
}

/** Shield outline used by the generic "security" illustration. */
fun shieldPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w * 0.5f, h * 0.03f)
        cubicTo(w * 0.66f, h * 0.11f, w * 0.82f, h * 0.13f, w * 0.94f, h * 0.14f)
        lineTo(w * 0.94f, h * 0.46f)
        cubicTo(w * 0.94f, h * 0.72f, w * 0.76f, h * 0.88f, w * 0.5f, h * 0.98f)
        cubicTo(w * 0.24f, h * 0.88f, w * 0.06f, h * 0.72f, w * 0.06f, h * 0.46f)
        lineTo(w * 0.06f, h * 0.14f)
        cubicTo(w * 0.18f, h * 0.13f, w * 0.34f, h * 0.11f, w * 0.5f, h * 0.03f)
        close()
    }
}
