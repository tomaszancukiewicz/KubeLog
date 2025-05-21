package com.kube.log.ui.compose.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int = 500,
    content: @Composable () -> Unit = {},
) {
    TooltipArea(
        tooltip = {
            ElevatedCard {
                Column(modifier = Modifier.padding(10.dp)) {
                    tooltip()
                }
            }
        },
        delayMillis = delayMillis,
        tooltipPlacement = TooltipPlacement.CursorPoint(
            alignment = Alignment.BottomEnd,
            offset = DpOffset(
                0.dp,
                4.dp
            )
        )
    ) {
        content()
    }
}