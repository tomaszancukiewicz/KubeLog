package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

val primary = Color(0xFF2962ff)
val primaryVariant = Color(0xFF0039cb)
val secondary = Color(0xFF29cdff)

val lightColors = lightColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = Color(0xFFE8E4ED),
    surface = Color(0xFFFFFFFF),
)

val darkColors = darkColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = Color(0xFF2C2F31),
    surface = Color(0xFF202020),
)

val typography = Typography().let {
    val fontSize1 = 14.sp
    val fontSize2 = 12.sp
    val fontSize3 = 10.sp

    it.copy(
        subtitle1 = it.subtitle1.copy(fontSize = fontSize1),
        subtitle2 = it.subtitle2.copy(fontSize = fontSize2),
        body1 = it.body1.copy(fontSize = fontSize1),
        body2 = it.body2.copy(fontSize = fontSize2),
        caption = it.caption.copy(fontSize = fontSize3),
        overline = it.overline.copy(fontSize = fontSize2)
    )
}

@Composable
fun ThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors else lightColors,
        typography = typography
    ) {
        content()
    }
}