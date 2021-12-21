package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.lightColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

val primary = Color(0xFF2962ff)
val primaryVariant = Color(0xFF0039cb)
val secondary = Color(0xFF00e5ff)

val lightColors = lightColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = Color(0xFFE8E4ED)
)

val darkColors = darkColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = Color(0xFF19151F)
)

@Composable
fun ThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    var isDark by remember { mutableStateOf(darkTheme) }

    MaterialTheme(
        colors = if (isDark) darkColors  else lightColors
    ) {
        Box {
            content()

            Button(onClick = {isDark = !isDark}, modifier = Modifier.align(Alignment.BottomEnd)) { Text("Change")}
        }
    }
}