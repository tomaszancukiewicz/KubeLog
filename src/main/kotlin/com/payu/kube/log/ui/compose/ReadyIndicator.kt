package com.payu.kube.log.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.theme.LocalCustomColorScheme

@Composable
fun ReadyIndicator(isReady: Boolean, modifier: Modifier = Modifier) {
    val customColorScheme = LocalCustomColorScheme.current

    Column(modifier = modifier.wrapContentSize(Alignment.Center)) {
        Box(
            modifier = Modifier
                .background(if (isReady) customColorScheme.green else customColorScheme.red, CircleShape)
                .defaultMinSize(12.dp, 12.dp)
        )
    }
}