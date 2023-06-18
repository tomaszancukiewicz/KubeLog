package com.payu.kube.log.ui.compose.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val lightColor = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

val darkColor = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

val typography = Typography().let {
    it.copy(
        displayLarge = it.displayLarge.copy(lineHeight = TextUnit.Unspecified, fontSize = 48.sp),
        displayMedium = it.displayMedium.copy(lineHeight = TextUnit.Unspecified, fontSize = 40.sp),
        displaySmall = it.displaySmall.copy(lineHeight = TextUnit.Unspecified, fontSize = 32.sp),
        headlineLarge = it.headlineLarge.copy(lineHeight = TextUnit.Unspecified, fontSize = 30.sp),
        headlineMedium = it.headlineMedium.copy(lineHeight = TextUnit.Unspecified, fontSize = 24.sp),
        headlineSmall = it.headlineSmall.copy(lineHeight = TextUnit.Unspecified, fontSize = 20.sp),
        titleLarge = it.titleLarge.copy(lineHeight = TextUnit.Unspecified, fontSize = 20.sp),
        titleMedium = it.titleMedium.copy(lineHeight = TextUnit.Unspecified, fontSize = 14.sp),
        titleSmall = it.titleSmall.copy(lineHeight = TextUnit.Unspecified, fontSize = 12.sp),
        bodyLarge = it.bodyLarge.copy(lineHeight = TextUnit.Unspecified, fontSize = 14.sp),
        bodyMedium = it.bodyMedium.copy(lineHeight = TextUnit.Unspecified, fontSize = 12.sp),
        bodySmall = it.bodySmall.copy(lineHeight = TextUnit.Unspecified, fontSize = 10.sp),
        labelLarge = it.labelLarge.copy(fontSize = 14.sp),
        labelMedium = it.labelMedium.copy(fontSize = 11.sp),
        labelSmall = it.labelSmall.copy(fontSize = 10.sp),
    )
}

val typography2 = toM2(typography)
val lightColors2 = toM2(lightColor, true)
val darkColors2 = toM2(darkColor, false)

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
            CustomColorSchemeProvider(
                darkTheme,
                content
            )
        }
    }
}