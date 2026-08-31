package com.yuyulife.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Cream,
    primaryContainer = WarmOrangeContainer,
    onPrimaryContainer = DeepBrown,
    background = Cream,
    surface = Cream,
    surfaceVariant = SoftCream,
)

private val DarkColors = darkColorScheme(
    primary = NightOrange,
    primaryContainer = NightOrangeContainer,
    background = NightBackground,
    surface = NightBackground,
    surfaceVariant = NightSurface,
)

@Composable
fun YuyuLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

