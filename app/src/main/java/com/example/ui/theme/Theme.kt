package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DtdcNavyLight,
    onPrimary = Color.White,
    primaryContainer = DtdcNavy,
    onPrimaryContainer = Color.White,
    secondary = DtdcRedLight,
    onSecondary = Color.White,
    secondaryContainer = DtdcRedDark,
    onSecondaryContainer = Color.White,
    tertiary = DtdcGold,
    background = Slate900,
    surface = Slate800,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DtdcNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EAFC),
    onPrimaryContainer = DtdcNavyDark,
    secondary = DtdcRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEBEE),
    onSecondaryContainer = DtdcRedDark,
    tertiary = DtdcGold,
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize authentic corporate DTDC brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
