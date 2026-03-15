package com.fitlife.app.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Colors ──────────────────────────────────────────────────────────────────

val FitGreen = Color(0xFF00C853)
val FitGreenDark = Color(0xFF00962C)
val FitGreenLight = Color(0xFF69F0AE)
val FitOrange = Color(0xFFFF6D00)
val FitOrangeLight = Color(0xFFFFAB40)
val FitBlue = Color(0xFF2979FF)
val FitRed = Color(0xFFFF1744)
val FitPurple = Color(0xFF651FFF)

val BackgroundDark = Color(0xFF0D1117)
val SurfaceDark = Color(0xFF161B22)
val SurfaceVariantDark = Color(0xFF21262D)
val CardDark = Color(0xFF1C2128)

// ─── Dark Color Scheme ────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = FitGreen,
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF005319),
    onPrimaryContainer = FitGreenLight,
    secondary = FitOrange,
    onSecondary = Color(0xFF3E1600),
    secondaryContainer = Color(0xFF5A2100),
    onSecondaryContainer = FitOrangeLight,
    tertiary = FitBlue,
    background = BackgroundDark,
    onBackground = Color(0xFFE6EDF3),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF8B949E),
    error = FitRed,
    onError = Color.White,
    outline = Color(0xFF30363D)
)

// ─── Light Color Scheme ───────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = FitGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F5C8),
    onPrimaryContainer = Color(0xFF002108),
    secondary = FitOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2F1500),
    tertiary = FitBlue,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Color(0xFF49454F),
    error = FitRed,
    onError = Color.White
)

// ─── Typography ───────────────────────────────────────────────────────────────

val FitLifeTypography = Typography()

// ─── Theme ────────────────────────────────────────────────────────────────────

@Composable
fun FitLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitLifeTypography,
        content = content
    )
}
