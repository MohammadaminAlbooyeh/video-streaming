package it.bollofacile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006875),
    primaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFF4A6367),
    secondaryContainer = Color(0xFFCCE8EC),
    tertiary = Color(0xFF38656C),
    tertiaryContainer = Color(0xFFBBEBF2),
    surface = Color(0xFFFCFDFD),
    error = Color(0xFFBA1A1A),
    onPrimary = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF67DBF4),
    primaryContainer = Color(0xFF00505B),
    secondary = Color(0xFFB0CBCF),
    secondaryContainer = Color(0xFF324B4E),
    tertiary = Color(0xFF9FD0D8),
    tertiaryContainer = Color(0xFF1E4C52),
    surface = Color(0xFF111D1E),
    error = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF001F22),
    onSurface = Color(0xFFE3E3E3)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}