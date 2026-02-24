package com.duda.app.presentation.common

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Cores do app ─────────────────────────────────────────────────────────────

// Updated palette: more color and contrast (still clean).
val DudaPrimary = Color(0xFF1565C0)   // Blue 800
val DudaSecondary = Color(0xFF1E88E5) // Blue 600
val DudaTertiary = Color(0xFF42A5F5)  // Blue 400
val DudaAccent = Color(0xFFFFA000)    // Amber 700

private val DarkColorScheme = darkColorScheme(
    primary = DudaAccent,
    secondary = DudaSecondary,
    tertiary = DudaTertiary,
    background = DudaPrimary,
    surface = DudaSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = DudaTertiary,
    secondary = DudaSecondary,
    tertiary = DudaPrimary,
    background = Color(0xFFF8F8FF),
    surface = Color(0xFFFFFFFF)
)

// ─── Tema principal ───────────────────────────────────────────────────────────

enum class AppTheme { SYSTEM, LIGHT, DARK }

@Composable
fun DudaTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    // Dynamic Color (Android 12+) can override the palette and leave the app looking "gray".
    // Default disabled to keep the app colorful.
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        // Dynamic Color disponível apenas no Android 12+
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
