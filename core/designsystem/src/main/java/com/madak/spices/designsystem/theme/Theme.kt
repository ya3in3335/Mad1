package com.madak.spices.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = MadakColors.Magenta,
    onPrimary = MadakColors.Paper,
    primaryContainer = MadakColors.MagentaSoft,
    onPrimaryContainer = MadakColors.MagentaDeep,
    secondary = MadakColors.Ink,
    onSecondary = MadakColors.OffWhite,
    secondaryContainer = MadakColors.Beige,
    onSecondaryContainer = MadakColors.Ink,
    tertiary = MadakColors.Gold,
    onTertiary = MadakColors.Ink,
    tertiaryContainer = MadakColors.GoldSoft,
    onTertiaryContainer = MadakColors.Ink,
    background = MadakColors.OffWhite,
    onBackground = MadakColors.Ink,
    surface = MadakColors.Paper,
    onSurface = MadakColors.Ink,
    surfaceVariant = MadakColors.Beige,
    onSurfaceVariant = MadakColors.Smoke.copy(alpha = 0.75f),
    surfaceContainer = MadakColors.Paper,
    surfaceContainerLow = MadakColors.OffWhite,
    surfaceContainerHigh = MadakColors.Beige.copy(alpha = 0.6f),
    outline = MadakColors.Gold.copy(alpha = 0.6f),
    outlineVariant = MadakColors.Beige,
    error = MadakColors.Paprika,
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFF5FA8),
    onPrimary = MadakColors.Ink,
    primaryContainer = MadakColors.MagentaDeep,
    onPrimaryContainer = MadakColors.MagentaSoft,
    secondary = MadakColors.OffWhite,
    onSecondary = MadakColors.Ink,
    secondaryContainer = MadakColors.Smoke,
    onSecondaryContainer = MadakColors.OffWhite,
    tertiary = MadakColors.Gold,
    onTertiary = MadakColors.Ink,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF4A3B22),
    onTertiaryContainer = MadakColors.GoldSoft,
    background = MadakColors.Ink,
    onBackground = MadakColors.OffWhite,
    surface = MadakColors.Charcoal,
    onSurface = MadakColors.OffWhite,
    surfaceVariant = MadakColors.Smoke,
    onSurfaceVariant = MadakColors.Beige.copy(alpha = 0.8f),
    surfaceContainer = MadakColors.Charcoal,
    surfaceContainerLow = MadakColors.Ink,
    surfaceContainerHigh = MadakColors.Smoke,
    outline = MadakColors.Gold.copy(alpha = 0.5f),
    outlineVariant = MadakColors.Smoke,
    error = androidx.compose.ui.graphics.Color(0xFFFF8A70),
)

val MadakShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun MadakTheme(
    rtl: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MadakTypography,
        shapes = MadakShapes,
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            content = content,
        )
    }
}
