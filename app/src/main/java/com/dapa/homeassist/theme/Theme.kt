package com.dapa.homeassist.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = LightBlue,
    background = DarkBg,
    surface = DarkBg
)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    secondary = NeonBlue,
    background = LightBg,
    surface = LightBg
)

@Composable
fun HomeAssistantDapaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
