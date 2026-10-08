package com.madak.spices.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.madak.spices.designsystem.R

enum class LogoPart(val res: Int, val ratio: Float) {
    FULL(R.drawable.madak_logo, 680f / 1093f),
    CHEF(R.drawable.madak_logo_chef, 680f / 734f),
    WORDMARK_TEXT(R.drawable.madak_logo_word, 670f / 332f),
    WORDMARK_LEAF(R.drawable.madak_logo_leaf, 670f / 332f),
}

/**
 * The official Madak logo, drawn from the brand artwork with its original proportions.
 * The artwork is white; [tint] is only used to render it on light surfaces.
 */
@Composable
fun MadakLogo(
    modifier: Modifier = Modifier,
    part: LogoPart = LogoPart.FULL,
    tint: androidx.compose.ui.graphics.Color? = null,
) {
    Image(
        painter = painterResource(part.res),
        contentDescription = "MADAK SPICES – مذاق لتوابل",
        modifier = modifier.aspectRatio(part.ratio),
        contentScale = ContentScale.Fit,
        colorFilter = tint?.let { ColorFilter.tint(it) },
    )
}

/** Wordmark with its green leaf, keeping the leaf colour even on light backgrounds. */
@Composable
fun MadakWordmark(modifier: Modifier = Modifier, textTint: androidx.compose.ui.graphics.Color? = null) {
    androidx.compose.foundation.layout.Box(modifier.aspectRatio(LogoPart.WORDMARK_TEXT.ratio)) {
        MadakLogo(part = LogoPart.WORDMARK_TEXT, tint = textTint)
        MadakLogo(part = LogoPart.WORDMARK_LEAF)
    }
}
