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

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = DeepViolet,
    tertiary = NeonPink,
    background = CosmicBg,
    surface = SurfaceDark,
    onPrimary = CosmicBg,
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight
)

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default
  dynamicColor: Boolean = false, // Disable dynamic colors to keep branded neon style intact
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
