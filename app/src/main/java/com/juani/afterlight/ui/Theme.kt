package com.juani.afterlight.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Light = lightColorScheme()
private val Dark = darkColorScheme()

@Composable
fun AfterlightTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Light, content = content)
}
