package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primary = Color(0xFF6200EE)
val primaryVariant = Color(0xFF3700B3)

@Composable
fun ThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = (
            if (darkTheme)
                darkColors(
                    primary = primary,
                    primaryVariant = primaryVariant
                )
            else
                lightColors(
                    primary = primary,
                    primaryVariant = primaryVariant
                )
        ),
        content = content
    )
}