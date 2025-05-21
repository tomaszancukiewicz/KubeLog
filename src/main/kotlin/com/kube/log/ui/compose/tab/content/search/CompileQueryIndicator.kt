package com.kube.log.ui.compose.tab.content.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kube.log.ui.compose.component.Tooltip
import com.kube.log.ui.compose.component.theme.LocalCustomColorScheme

@Composable
fun CompileQueryIndicator(
    queryErrors: List<String>?
) {
    val customColorScheme = LocalCustomColorScheme.current

    Tooltip(
        tooltip = {
            Text(
                text = queryErrors
                    ?.takeIf { it.isNotEmpty() }
                    ?.joinToString("\n", "Literal mode.\nNot interpreted because of errors:\n") {
                        " - $it"
                    } ?: "Interpreted mode"
            )
        },
        delayMillis = 0,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (queryErrors.isNullOrEmpty()) customColorScheme.blue
                    else customColorScheme.green,
                    CircleShape
                )
        )
    }
}