package com.yourtech.systeme.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourtech.systeme.designsystem.R

/** YOURTECH SYSTEME brand palette. */
object YT {
    val Navy = Color(0xFF071426)
    val NavyDeep = Color(0xFF040C18)
    val Surface = Color(0xFF101F35)
    val SurfaceHigh = Color(0xFF16294A)
    val Outline = Color(0xFF223A5E)
    val Blue = Color(0xFF176BFF)
    val Cyan = Color(0xFF00D9FF)
    val White = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF9BB0CC)
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF5A524)
    val Danger = Color(0xFFFF5470)
    val Violet = Color(0xFF7C5CFF)
    val Glass = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x26FFFFFF)
}

val Tajawal = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, line: Int = (size * 1.35f).toInt(), spacing: Float = 0f) =
    TextStyle(fontFamily = Tajawal, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = spacing.sp)

val YTTypography = Typography(
    displaySmall = style(34, FontWeight.ExtraBold),
    headlineLarge = style(30, FontWeight.ExtraBold),
    headlineMedium = style(26, FontWeight.Bold),
    headlineSmall = style(22, FontWeight.Bold),
    titleLarge = style(20, FontWeight.Bold),
    titleMedium = style(17, FontWeight.Bold),
    titleSmall = style(15, FontWeight.Medium),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 21),
    bodySmall = style(12, FontWeight.Normal, 17),
    labelLarge = style(15, FontWeight.Bold),
    labelMedium = style(13, FontWeight.Medium),
    labelSmall = style(11, FontWeight.Medium, spacing = 0.3f),
)

private val Scheme = darkColorScheme(
    primary = YT.Blue,
    onPrimary = YT.White,
    primaryContainer = Color(0xFF0E3A8A),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = YT.Cyan,
    onSecondary = YT.Navy,
    secondaryContainer = Color(0xFF00394A),
    onSecondaryContainer = Color(0xFFB8F4FF),
    tertiary = YT.Violet,
    background = YT.Navy,
    onBackground = YT.White,
    surface = YT.Surface,
    onSurface = YT.White,
    surfaceVariant = YT.SurfaceHigh,
    onSurfaceVariant = YT.TextMuted,
    surfaceContainer = YT.Surface,
    surfaceContainerHigh = YT.SurfaceHigh,
    surfaceContainerLow = YT.Navy,
    surfaceContainerLowest = YT.NavyDeep,
    surfaceContainerHighest = Color(0xFF1C3358),
    outline = YT.Outline,
    outlineVariant = Color(0xFF18304F),
    error = YT.Danger,
)

val YTShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** The brand is dark-first (security control-centre look) in every system theme. */
@Composable
fun YourTechTheme(rtl: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = YTTypography, shapes = YTShapes) {
        CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr, content = content)
    }
}
