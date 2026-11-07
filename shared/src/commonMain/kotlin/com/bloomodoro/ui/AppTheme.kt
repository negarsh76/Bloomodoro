package com.bloomodoro.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloomodoro.model.ColorTheme

private fun Long.toColor(): Color = Color(this)

@Composable
fun BloomodoroTheme(theme: ColorTheme, content: @Composable () -> Unit) {
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            background = theme.background.toColor(),
            surface = theme.surface.toColor(),
            primary = theme.primary.toColor(),
            secondary = theme.secondary.toColor(),
            tertiary = theme.accentBloom.toColor(),
            onBackground = Color(0xFFF3EAF2),
            onSurface = Color(0xFFF3EAF2),
        )
    } else {
        lightColorScheme(
            background = theme.background.toColor(),
            surface = theme.surface.toColor(),
            primary = theme.primary.toColor(),
            secondary = theme.secondary.toColor(),
            tertiary = theme.accentBloom.toColor(),
            onBackground = Color(0xFF2B2129),
            onSurface = Color(0xFF2B2129),
        )
    }

    // Generously rounded everywhere — this is the one consistent "signature" of the app's
    // personality, paired with the bloom companion, instead of scattering many small effects.
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )

    val typography = Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
            headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = bodyLarge.copy(lineHeight = 22.sp),
        )
    }

    MaterialTheme(colorScheme = colorScheme, shapes = shapes, typography = typography, content = content)
}

/** The big countdown number gets its own scaled style — bold and friendly, never sharp/technical. */
val TimerDigitsStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 64.sp, letterSpacing = (-1).sp)
