package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

fun toM2(colorScheme: ColorScheme, isDark: Boolean) = Colors(
    colorScheme.primary,
    colorScheme.secondary,
    colorScheme.tertiary,
    colorScheme.tertiary,
    colorScheme.background,
    colorScheme.surface,
    colorScheme.error,
    colorScheme.onPrimary,
    colorScheme.onSecondary,
    colorScheme.onBackground,
    colorScheme.onSurface,
    colorScheme.onError,
    isDark
)

val primary = Color(0xFF2962ff)
val secondary = Color(0xFF0039cb)
val tertiary = Color(0xFF29cdff)

val lightColor = lightColorScheme(
    primary = primary,
    secondary = secondary,
    tertiary = tertiary,
    background = Color(0xFFE8E4ED),
    surface = Color(0xFFFFFFFF),
)

val lightColors2 = toM2(lightColor, true)

val darkColor = darkColorScheme(
    primary = primary,
    secondary = secondary,
    tertiary = tertiary,
    background = Color(0xFF2C2F31),
    surface = Color(0xFF202020),
)

val darkColors2 = toM2(darkColor, false)

val typography = Typography().let {
    val fontSize1 = 14.sp
    val fontSize2 = 12.sp
    val fontSize3 = 10.sp

    it.copy(
        titleMedium = it.titleMedium.copy(fontSize = fontSize1, lineHeight = TextUnit.Unspecified),
        titleSmall = it.titleSmall.copy(fontSize = fontSize2, lineHeight = TextUnit.Unspecified),
        bodyLarge = it.bodyLarge.copy(fontSize = fontSize1, lineHeight = TextUnit.Unspecified),
        bodyMedium = it.bodyMedium.copy(fontSize = fontSize2, lineHeight = TextUnit.Unspecified),
        bodySmall = it.bodySmall.copy(fontSize = fontSize3, lineHeight = TextUnit.Unspecified),
        labelSmall = it.labelSmall.copy(fontSize = fontSize2, lineHeight = TextUnit.Unspecified)
    )
}

val typography2 = androidx.compose.material.Typography(
    h1 = typography.displayLarge,
    h2 = typography.displayMedium,
    h3 = typography.displaySmall,
    h4 = typography.headlineMedium,
    h5 = typography.headlineSmall,
    h6 = typography.titleLarge,
    subtitle1 = typography.titleMedium,
    subtitle2 = typography.titleSmall,
    body1 = typography.bodyLarge,
    body2 = typography.bodyMedium,
    caption = typography.bodySmall,
    button = typography.labelLarge,
    overline = typography.labelSmall
)

@Composable
fun ThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColor else lightColor,
        typography = typography
    ) {
        androidx.compose.material.MaterialTheme(
            colors = if (darkTheme) darkColors2 else lightColors2,
            typography = typography2
        ) {
            content()
        }
    }
}