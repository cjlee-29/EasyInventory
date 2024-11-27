package com.example.easyinventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your light and dark color schemes here
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = AccentColor,
    error = ErrorButtonColor,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onError = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = AccentColor,
    error = AccentColor,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onError = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun EasyInventoryTheme(
    darkTheme: Boolean = false, // Change it to true for dark theme
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}