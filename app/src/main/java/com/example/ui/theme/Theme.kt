package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FantasyDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBg,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = GoldLight,
    secondary = CrimsonPrimary,
    onSecondary = FantasyText,
    secondaryContainer = DarkPanelLight,
    onSecondaryContainer = FantasyText,
    tertiary = GoldLight,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = FantasyText,
    surface = DarkPanel,
    onSurface = FantasyText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = FantasyTextDim,
    outline = DarkCardBorder,
    error = CrimsonLight,
    onError = FantasyText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FantasyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
