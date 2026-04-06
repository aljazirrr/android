package com.bariatric.assistant.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = MedicalGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MedicalGreenContainer,
    onPrimaryContainer = OnMedicalGreenContainer,
    secondary = CalmBlue,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = CalmBlueContainer,
    onSecondaryContainer = OnCalmBlueContainer,
    tertiary = WarmOrange,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = WarmOrangeContainer,
    onTertiaryContainer = OnWarmOrangeContainer,
    error = ErrorRed,
    errorContainer = ErrorRedContainer,
    onErrorContainer = OnErrorRedContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = MedicalGreenLight,
    onPrimary = OnMedicalGreenContainer,
    primaryContainer = MedicalGreenDark,
    onPrimaryContainer = MedicalGreenContainer,
    secondary = CalmBlueLight,
    onSecondary = OnCalmBlueContainer,
    secondaryContainer = CalmBlueDark,
    onSecondaryContainer = CalmBlueContainer,
    tertiary = WarmOrange,
    onTertiary = OnWarmOrangeContainer,
    tertiaryContainer = WarmOrange,
    onTertiaryContainer = WarmOrangeContainer,
    error = ErrorRedContainer,
    errorContainer = ErrorRed,
    onErrorContainer = ErrorRedContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun BariatricAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
