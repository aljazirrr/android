package com.fooddiary.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─── Colors ──────────────────────────────────────────────────────────────────

val Green = Color(0xFF4CAF50)
val GreenDark = Color(0xFF388E3C)
val GreenLight = Color(0xFFA5D6A7)
val Orange = Color(0xFFFF9800)
val OrangeLight = Color(0xFFFFCC80)
val Blue = Color(0xFF2196F3)
val Red = Color(0xFFF44336)
val Purple = Color(0xFF9C27B0)

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val CardDark = Color(0xFF2C2C2C)

// ─── Color Schemes ───────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenLight,
    secondary = Orange,
    onSecondary = Color.White,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = CardDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0)
)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = Orange,
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF666666)
)

@Composable
fun FoodDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
