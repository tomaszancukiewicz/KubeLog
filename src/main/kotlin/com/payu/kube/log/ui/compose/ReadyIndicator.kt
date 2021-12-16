package com.payu.kube.log.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReadyIndicator(isReady: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.wrapContentSize(Alignment.Center)) {
        Box(
            modifier = Modifier
                .background(if (isReady) Color(0xff2bc140) else Color(0xfff55e56), CircleShape)
                .size(12.dp)
        )
    }
}