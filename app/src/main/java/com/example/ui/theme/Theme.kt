package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TvPrimary,
    secondary = TvSecondary,
    background = TvBackground,
    surface = TvSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TvOnBackground,
    onSurface = TvOnSurface,
    error = TvMutedRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
