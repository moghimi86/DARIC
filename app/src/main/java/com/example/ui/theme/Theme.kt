package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomDarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = NeonPurple,
    tertiary = NeonYellowGreen,
    background = DeepDarkBackground,
    surface = SurfaceGlass,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DeepDarkBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = RedNegative
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
