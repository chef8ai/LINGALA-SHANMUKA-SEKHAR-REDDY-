package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = PurePaperWhite,
    secondary = ApricotAccent,
    onSecondary = PurePaperWhite,
    tertiary = NeonEmerald,
    background = CosmicDarkBackground,
    onBackground = CosmicBackgroundLight,
    surface = CosmicDarkSurface,
    onSurface = CosmicBackgroundLight,
    surfaceVariant = CosmicDarkCard,
    onSurfaceVariant = PurePaperWhite
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = PurePaperWhite,
    secondary = ApricotAccent,
    onSecondary = PurePaperWhite,
    tertiary = NeonEmerald,
    background = CosmicBackgroundLight,
    onBackground = CosmicDarkBackground,
    surface = PurePaperWhite,
    onSurface = CosmicDarkBackground,
    surfaceVariant = IndigoLight,
    onSurfaceVariant = IndigoDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable to force our custom beautiful brand colors!
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
