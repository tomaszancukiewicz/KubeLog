package com.payu.kube.log.ui.compose.tab.content.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.theme.LocalCustomColorScheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompileQueryIndicator(
    queryErrors: List<String>?
) {
    val customColorScheme = LocalCustomColorScheme.current

    TooltipArea(
        tooltip = {
            ElevatedCard {
                Text(
                    text = queryErrors
                        ?.takeIf { it.isNotEmpty() }
                        ?.joinToString("\n", "Literal mode.\nNot interpreted because of errors:\n") {
                            " - $it"
                        } ?: "Interpreted mode",
                    modifier = Modifier.padding(10.dp)
                )
            }
        },
        delayMillis = 0,
        tooltipPlacement = TooltipPlacement.CursorPoint()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (queryErrors.isNullOrEmpty()) customColorScheme.blue
                    else customColorScheme.green,
                    CircleShape
                )
        )
    }
}