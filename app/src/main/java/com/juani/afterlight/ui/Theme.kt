package com.juani.afterlight.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AfterlightLight = lightColorScheme(
    primary = Color(0xFF6F4D20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF6DDB7),
    onPrimaryContainer = Color(0xFF251A09),
    secondary = Color(0xFF665A49),
    secondaryContainer = Color(0xFFEEDFC8),
    tertiary = Color(0xFF4C6545),
    tertiaryContainer = Color(0xFFCFEBC3),
    background = Color(0xFFFFFBF6),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBF6),
    surfaceContainerLow = Color(0xFFF8F0E7),
    surfaceContainerHigh = Color(0xFFEFE6DC),
    surfaceContainerHighest = Color(0xFFE8DED3),
)

private val AfterlightDark = darkColorScheme(
    primary = Color(0xFFE9C48D),
    onPrimary = Color(0xFF3F2D10),
    primaryContainer = Color(0xFF5A421B),
    onPrimaryContainer = Color(0xFFF6DDB7),
    secondary = Color(0xFFD5C3AB),
    tertiary = Color(0xFFB3CEA9),
    background = Color(0xFF11100E),
    onBackground = Color(0xFFEDE4D4),
    surface = Color(0xFF11100E),
    surfaceContainerLow = Color(0xFF1B1814),
    surfaceContainerHigh = Color(0xFF27231D),
    surfaceContainerHighest = Color(0xFF322D25),
)

@Composable
fun AfterlightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AfterlightLight,
        typography = Typography(),
        content = content,
    )
}
