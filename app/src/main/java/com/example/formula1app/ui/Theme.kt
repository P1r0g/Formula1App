package com.example.formula1app.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.formula1app.data.AppState

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondaryText,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightText,

    tertiary = LightPrimary,
    onTertiary = LightOnPrimary,
    tertiaryContainer = LightPrimaryContainer,
    onTertiaryContainer = LightOnPrimaryContainer,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightSecondaryText,

    outline = LightSecondaryText,
    outlineVariant = LightOutline,

    inverseSurface = DarkSurface,
    inverseOnSurface = DarkText,
    inversePrimary = DarkPrimary,

    surfaceTint = Color.Transparent,
    surfaceDim = LightBackground,
    surfaceBright = LightSurface,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondaryText,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,

    tertiary = DarkPrimary,
    onTertiary = DarkOnPrimary,
    tertiaryContainer = DarkPrimaryContainer,
    onTertiaryContainer = DarkOnPrimaryContainer,

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryText,

    outline = DarkSecondaryText,
    outlineVariant = DarkOutline,

    inverseSurface = LightSurface,
    inverseOnSurface = LightText,
    inversePrimary = LightPrimary,

    surfaceTint = Color.Transparent,
    surfaceDim = DarkBackground,
    surfaceBright = DarkSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceVariant
)

@Composable
fun Formula1AppTheme(
    darkTheme: Boolean = AppState.darkTheme ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window

            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    AppLocalization {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}