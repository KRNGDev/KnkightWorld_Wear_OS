package com.knightworld.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors
import androidx.compose.ui.graphics.Color

private val colorPalette = Colors(
    primary = Gold,
    primaryVariant = Slate,
    secondary = Ember,
    secondaryVariant = Ember,
    background = NightSky,
    surface = Slate,
    error = Color(0xFFEF5350),
    onPrimary = OnGold,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White,
    isLight = false
)

@Composable
fun KnightWorldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = colorPalette,
        typography = androidx.wear.compose.material.Typography(
            display1 = KnightTypography.displayLarge,
            display2 = KnightTypography.displayMedium,
            body1 = KnightTypography.bodyLarge,
            body2 = KnightTypography.bodyMedium,
            caption1 = KnightTypography.bodySmall
        ),
        content = content
    )
}
