package com.yandelaroli.buscalocal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8CF8CE),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4C635A),
    secondaryContainer = Color(0xFFCFE9DC),
    background = Color(0xFFF7FBF8),
    surface = Color(0xFFF7FBF8),
    surfaceVariant = Color(0xFFDBE5DF),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF70DBB3),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFF8CF8CE),
    secondary = Color(0xFFB4CCC0),
    secondaryContainer = Color(0xFF354B42),
    background = Color(0xFF101411),
    surface = Color(0xFF101411),
    surfaceVariant = Color(0xFF404943),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BuscaLocalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
