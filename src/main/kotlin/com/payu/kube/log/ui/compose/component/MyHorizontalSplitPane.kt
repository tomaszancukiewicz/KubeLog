package com.payu.kube.log.ui.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState

@ExperimentalSplitPaneApi
@Composable
fun MyHorizontalSplitPane(
    splitPaneState: SplitPaneState,
    firstColumnCompose: (@Composable () -> Unit)? = null,
    secondColumnCompose: (@Composable () -> Unit)? = null
) {
    when {
        firstColumnCompose != null && secondColumnCompose != null -> {
            HorizontalSplitPane(splitPaneState = splitPaneState) {
                first(minSize = 250.dp) {
                    firstColumnCompose()
                }
                second(minSize = 200.dp) {
                    secondColumnCompose()
                }
            }
        }
        firstColumnCompose != null -> firstColumnCompose()
        secondColumnCompose != null -> secondColumnCompose()
    }
}