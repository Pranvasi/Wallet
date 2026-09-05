package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val SquircleShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Fallback schemes when Dynamic Color (Android 12+) is unavailable
private val FallbackDarkScheme = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF002D6D),
    primaryContainer = Color(0xFF00439B),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFBFC6DC),
    background = Color(0xFF111318),
    surface = Color(0xFF111318)
)

private val FallbackLightScheme = lightColorScheme(
    primary = Color(0xFF1E5BB8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = Color(0xFF575E71),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF)
)

@Composable
fun WalletTheme(
    themeMode: String = "system",
    themePalette: String = "monet",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "light" -> false
        "dark", "amoled" -> true
        else -> isSystemDark
    }

    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // App strictly follows Dynamic color theming (Material You)
    var scheme: ColorScheme = if (supportsDynamic) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) FallbackDarkScheme else FallbackLightScheme
    }

    // If AMOLED mode is enabled, set background & surface to pure pitch black
    if (themeMode == "amoled") {
        scheme = scheme.copy(
            background = Color.Black,
            surface = Color(0xFF070707),
            surfaceContainer = Color(0xFF121212)
        )
    }

    // Dynamic Status Bar & Navigation Bar Icon Colors for Light/Dark mode
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In Light mode (!isDark == true), status bar icons should be dark
                // In Dark mode (!isDark == false), status bar icons should be light
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = SquircleShapes,
        content = content
    )
}
