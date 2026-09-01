package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
  ELEGANT_DARK,
  AMOLED_BLACK,
  SLATE_DARK,
  LIGHT,
  SYSTEM
}

private val ElegantDarkColorScheme = darkColorScheme(
  primary = ElegantPrimary,
  onPrimary = ElegantOnPrimary,
  primaryContainer = ElegantPrimary,
  onPrimaryContainer = ElegantOnPrimary,
  secondary = ElegantAccentBlue,
  onSecondary = ElegantOnPrimary,
  secondaryContainer = ElegantDarkCardHover,
  onSecondaryContainer = ElegantTextPrimary,
  background = ElegantDarkBg,
  onBackground = ElegantTextPrimary,
  surface = ElegantDarkSurface,
  onSurface = ElegantTextPrimary,
  surfaceVariant = ElegantDarkCard,
  onSurfaceVariant = ElegantTextSecondary,
  outline = ElegantDarkBorder,
  error = RedDelete,
  onError = Color.White
)

private val AmoledColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF003833),
  onPrimaryContainer = CyanAccent,
  secondary = OrangePrimary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF4A1E00),
  onSecondaryContainer = AmberAccent,
  background = AmoledBlack,
  onBackground = Color(0xFFECEFF1),
  surface = AmoledSurface,
  onSurface = Color(0xFFECEFF1),
  surfaceVariant = AmoledCard,
  onSurfaceVariant = Color(0xFFB0BEC5),
  outline = AmoledBorder,
  error = RedDelete,
  onError = Color.White
)

private val SlateColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF0D3E3A),
  onPrimaryContainer = CyanAccent,
  secondary = OrangePrimary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF42210C),
  onSecondaryContainer = AmberAccent,
  background = SlateDarkBackground,
  onBackground = Color(0xFFF1F5F9),
  surface = SlateDarkSurface,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = SlateDarkCard,
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = SlateDarkBorder,
  error = RedDelete,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF00897B),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2F1),
  onPrimaryContainer = Color(0xFF004D40),
  secondary = Color(0xFFE65100),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFE0B2),
  onSecondaryContainer = Color(0xFFBF360C),
  background = LightBackground,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightCard,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  error = RedDelete,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.ELEGANT_DARK,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val effectiveTheme = when (themeMode) {
    AppThemeMode.ELEGANT_DARK -> AppThemeMode.ELEGANT_DARK
    AppThemeMode.AMOLED_BLACK -> AppThemeMode.AMOLED_BLACK
    AppThemeMode.SLATE_DARK -> AppThemeMode.SLATE_DARK
    AppThemeMode.LIGHT -> AppThemeMode.LIGHT
    AppThemeMode.SYSTEM -> if (isSystemDark) AppThemeMode.ELEGANT_DARK else AppThemeMode.LIGHT
  }

  val context = LocalContext.current
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (effectiveTheme == AppThemeMode.LIGHT) dynamicLightColorScheme(context)
      else dynamicDarkColorScheme(context)
    }
    effectiveTheme == AppThemeMode.ELEGANT_DARK -> ElegantDarkColorScheme
    effectiveTheme == AppThemeMode.AMOLED_BLACK -> AmoledColorScheme
    effectiveTheme == AppThemeMode.SLATE_DARK -> SlateColorScheme
    else -> LightColorScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val statusBarColor = when (effectiveTheme) {
          AppThemeMode.ELEGANT_DARK -> ElegantDarkBg.toArgb()
          AppThemeMode.AMOLED_BLACK -> AmoledBlack.toArgb()
          AppThemeMode.SLATE_DARK -> SlateDarkBackground.toArgb()
          AppThemeMode.LIGHT -> LightBackground.toArgb()
          AppThemeMode.SYSTEM -> if (isSystemDark) ElegantDarkBg.toArgb() else LightBackground.toArgb()
        }
        window.statusBarColor = statusBarColor
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
          (effectiveTheme == AppThemeMode.LIGHT)
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
