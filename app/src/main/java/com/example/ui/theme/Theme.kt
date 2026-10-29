package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GripDarkColorScheme = darkColorScheme(
    primary = FlameOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C1608),
    onPrimaryContainer = Color(0xFFFFCCBC),
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF072733),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = FlameOrangeVariant,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = SlateLight,
    surface = DarkSurface,
    onSurface = SlateLight,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = SlateMuted,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E2532)
)

@Composable
fun GripForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GripDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GripForgeTheme(content = content)
}
