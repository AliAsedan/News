package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentGreen,
    secondary = PrimaryGreenLight,
    tertiary = AccentGreen,
    background = DarkGreenBackground,
    surface = DarkGreenSurface,
    onPrimary = DarkGreenBackground,
    onSecondary = PureWhite,
    onBackground = DarkGreenText,
    onSurface = DarkGreenText,
    outline = SlateTextLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreenLight,
    tertiary = AccentGreen,
    background = SoftBackground,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PrimaryGreen,
    onBackground = SlateTextDark,
    onSurface = SlateTextDark,
    outline = CardBorderGreen
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamicColor by default to prioritize our white & green custom theme
  dynamicColor: Boolean = false,
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
