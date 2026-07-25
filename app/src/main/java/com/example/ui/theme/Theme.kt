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

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = BoldPrimaryContainer,
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = BoldSecondaryContainer,
    background = Color(0xFF141218),
    surface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFF49454F),
    onBackground = Color(0xFFE6E0E9),
    onSurface = Color(0xFFE6E0E9),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimary,
    onPrimary = BoldOnPrimary,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = BoldOnPrimaryContainer,
    secondary = BoldSecondary,
    onSecondary = BoldOnPrimary,
    secondaryContainer = BoldSecondaryContainer,
    onSecondaryContainer = BoldOnSecondaryContainer,
    background = BoldBackground,
    surface = BoldSurface,
    surfaceVariant = BoldSurfaceVariant,
    onBackground = BoldOnBackground,
    onSurface = BoldOnSurface,
    onSurfaceVariant = BoldOnSurfaceVariant,
    outline = BoldOutline
  )

@Composable
fun FileManagerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use strict Bold Typography custom brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


