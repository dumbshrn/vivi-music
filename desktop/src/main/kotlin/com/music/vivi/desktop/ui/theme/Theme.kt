package com.music.vivi.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

val DefaultSeed = Color(0xFFED5564)

private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    line: Int,
    letter: Double,
): TextStyle =
    TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = letter.sp,
    )

/**
 * Material 3 Expressive type scale, copied from the Android app's
 * `getTypography` so headlines stay Regular weight instead of being bolded.
 */
private fun viviTypography(family: FontFamily = FontFamily.Default): Typography =
    Typography(
        displayLarge = style(family, FontWeight.Normal, 57, 64, -0.25),
        displayMedium = style(family, FontWeight.Normal, 45, 52, 0.0),
        displaySmall = style(family, FontWeight.Normal, 36, 44, 0.0),
        headlineLarge = style(family, FontWeight.Normal, 32, 40, 0.0),
        headlineMedium = style(family, FontWeight.Normal, 28, 36, 0.0),
        headlineSmall = style(family, FontWeight.Normal, 24, 32, 0.0),
        titleLarge = style(family, FontWeight.Normal, 22, 28, 0.0),
        titleMedium = style(family, FontWeight.Medium, 16, 24, 0.15),
        titleSmall = style(family, FontWeight.Medium, 14, 20, 0.1),
        bodyLarge = style(family, FontWeight.Normal, 16, 24, 0.5),
        bodyMedium = style(family, FontWeight.Normal, 14, 20, 0.25),
        bodySmall = style(family, FontWeight.Normal, 12, 16, 0.4),
        labelLarge = style(family, FontWeight.Medium, 14, 20, 0.1),
        labelMedium = style(family, FontWeight.Medium, 12, 16, 0.5),
        labelSmall = style(family, FontWeight.Medium, 11, 16, 0.5),
    )

private fun ColorScheme.withPureBlack(): ColorScheme =
    copy(
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF0A0A0A),
        surfaceContainer = Color(0xFF101010),
        surfaceContainerHigh = Color(0xFF161616),
        surfaceContainerHighest = Color(0xFF1C1C1C),
        background = Color.Black,
    )

@Composable
fun ViviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seed: Color = DefaultSeed,
    pureBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val base =
        rememberDynamicColorScheme(
            seedColor = seed,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = if (seed.toArgb() == 0xFF000000.toInt()) PaletteStyle.Monochrome else PaletteStyle.TonalSpot,
        )
    val colorScheme = if (darkTheme && pureBlack) base.withPureBlack() else base
    val typography = remember { viviTypography() }
    val shapes =
        remember {
            Shapes(
                extraSmall = RoundedCornerShape(4.dp),
                small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(16.dp),
                extraLarge = RoundedCornerShape(28.dp),
            )
        }

    MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes, content = content)
}

/** Material 3 "expressive" style emphasis used across VIVI screens. */
val HeroTextStyle = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
