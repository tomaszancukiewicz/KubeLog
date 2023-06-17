package com.payu.kube.log.ui.compose.component.theme

import androidx.compose.material.Colors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography

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

fun toM2(typography: Typography) = androidx.compose.material.Typography(
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