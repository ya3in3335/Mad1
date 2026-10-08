package com.madak.spices.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.madak.spices.designsystem.R

/** Tajawal (SIL OFL) covers both Arabic and Latin scripts. */
val Tajawal = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, line: Int = (size * 1.35f).toInt(), spacing: Float = 0f) =
    TextStyle(fontFamily = Tajawal, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = spacing.sp)

val MadakTypography = Typography(
    displayLarge = style(48, FontWeight.ExtraBold),
    displayMedium = style(40, FontWeight.ExtraBold),
    displaySmall = style(34, FontWeight.Bold),
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
    labelSmall = style(11, FontWeight.Medium, spacing = 0.2f),
)
