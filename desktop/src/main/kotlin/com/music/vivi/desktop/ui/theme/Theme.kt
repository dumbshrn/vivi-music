package com.music.vivi.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

val DefaultSeed = Color(0xFFED5564)

private fun viviTypography(): Typography {
    val base = Typography()
    val family = FontFamily.Default
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = family, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family, letterSpacing = 0.5.sp),
    )
}

@Composable
fun ViviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seed: Color = DefaultSeed,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        rememberDynamicColorScheme(
            seedColor = seed,
            isDark = darkTheme,
            style = PaletteStyle.TonalSpot,
        )
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
val HeroTextStyle = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
