package com.payu.kube.log.ui.compose.component.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class CustomColorScheme(
    val green: Color,
    val onGreen: Color,
    val greenContainer: Color,
    val onGreenContainer: Color,
    val red: Color,
    val onRed: Color,
    val redContainer: Color,
    val onRedContainer: Color,
    val blue: Color,
    val onBlue: Color,
    val blueContainer: Color,
    val onBlueContainer: Color,
)

val lightCustomColorScheme = CustomColorScheme(
    green = light_Green,
    onGreen = light_onGreen,
    greenContainer = light_GreenContainer,
    onGreenContainer = light_onGreenContainer,
    red = light_Red,
    onRed = light_onRed,
    redContainer = light_RedContainer,
    onRedContainer = light_onRedContainer,
    blue = light_Blue,
    onBlue = light_onBlue,
    blueContainer = light_BlueContainer,
    onBlueContainer = light_onBlueContainer,
)

val darkCustomColorScheme = CustomColorScheme(
    green = dark_Green,
    onGreen = dark_onGreen,
    greenContainer = dark_GreenContainer,
    onGreenContainer = dark_onGreenContainer,
    red = dark_Red,
    onRed = dark_onRed,
    redContainer = dark_RedContainer,
    onRedContainer = dark_onRedContainer,
    blue = dark_Blue,
    onBlue = dark_onBlue,
    blueContainer = dark_BlueContainer,
    onBlueContainer = dark_onBlueContainer,
)

val LocalCustomColorScheme = staticCompositionLocalOf { lightCustomColorScheme }

@Composable
fun CustomColorSchemeProvider(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val customColorScheme = if(darkTheme) darkCustomColorScheme else lightCustomColorScheme

    CompositionLocalProvider(
        LocalCustomColorScheme provides customColorScheme,
        content = content
    )
}

