package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class SuitTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val glowColor: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val surfaceElevated: Color,
    val cardBorder: Color
) {
    MARK_LXXXV(
        id = "mark_85",
        title = "MARK LXXXV",
        subtitle = "Arc Cyan & Obsidian (Classic)",
        primaryColor = JarvisCyan,
        secondaryColor = JarvisSky,
        accentColor = JarvisGold,
        glowColor = JarvisGlowCyan,
        darkBackground = JarvisDarkBackground,
        darkSurface = JarvisDarkSurface,
        surfaceElevated = JarvisSurfaceElevated,
        cardBorder = JarvisCardBorder
    ),
    MARK_L_CRIMSON(
        id = "mark_50",
        title = "MARK L NANOTECH",
        subtitle = "Crimson Pulse & Stark Gold",
        primaryColor = StarkCrimson,
        secondaryColor = StarkGold,
        accentColor = StarkAmber,
        glowColor = StarkCrimsonGlow,
        darkBackground = StarkCrimsonDarkBg,
        darkSurface = StarkCrimsonSurface,
        surfaceElevated = StarkCrimsonElevated,
        cardBorder = StarkCrimsonBorder
    ),
    STEALTH_OPS(
        id = "stealth_ops",
        title = "STEALTH TACTICAL",
        subtitle = "Cyber Emerald & Matrix Jade",
        primaryColor = StealthEmerald,
        secondaryColor = StealthMint,
        accentColor = StealthJade,
        glowColor = StealthEmeraldGlow,
        darkBackground = StealthDarkBg,
        darkSurface = StealthSurface,
        surfaceElevated = StealthElevated,
        cardBorder = StealthBorder
    ),
    WAR_MACHINE(
        id = "war_machine",
        title = "WAR MACHINE",
        subtitle = "Titanium Violet & Plasma",
        primaryColor = WarViolet,
        secondaryColor = WarLavender,
        accentColor = WarPlasma,
        glowColor = WarVioletGlow,
        darkBackground = WarDarkBg,
        darkSurface = WarSurface,
        surfaceElevated = WarElevated,
        cardBorder = WarBorder
    )
}

val LocalSuitTheme = staticCompositionLocalOf { SuitTheme.MARK_LXXXV }

fun buildColorSchemeForSuit(suitTheme: SuitTheme): ColorScheme {
    return darkColorScheme(
        primary = suitTheme.primaryColor,
        onPrimary = Color(0xFF030712),
        primaryContainer = suitTheme.surfaceElevated,
        onPrimaryContainer = suitTheme.primaryColor,
        secondary = suitTheme.secondaryColor,
        onSecondary = Color(0xFF030712),
        secondaryContainer = suitTheme.darkSurface,
        onSecondaryContainer = suitTheme.secondaryColor,
        tertiary = suitTheme.accentColor,
        onTertiary = Color(0xFF030712),
        tertiaryContainer = suitTheme.darkSurface,
        onTertiaryContainer = suitTheme.accentColor,
        background = suitTheme.darkBackground,
        onBackground = JarvisTextPrimary,
        surface = suitTheme.darkSurface,
        onSurface = JarvisTextPrimary,
        surfaceVariant = suitTheme.surfaceElevated,
        onSurfaceVariant = JarvisTextSecondary,
        outline = suitTheme.cardBorder,
        outlineVariant = suitTheme.primaryColor.copy(alpha = 0.3f),
        error = JarvisRedAlert,
        onError = Color.White
    )
}

@Composable
fun MyApplicationTheme(
    suitTheme: SuitTheme = SuitTheme.MARK_LXXXV,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = buildColorSchemeForSuit(suitTheme)
    CompositionLocalProvider(LocalSuitTheme provides suitTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
