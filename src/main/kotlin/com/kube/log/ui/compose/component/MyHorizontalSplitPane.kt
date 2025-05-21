package com.kube.log.ui.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import java.awt.Cursor

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun MyHorizontalSplitPane(
    splitPaneState: SplitPaneState,
    firstColumnCompose: (@Composable () -> Unit)? = null,
    secondColumnCompose: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when {
        firstColumnCompose != null && secondColumnCompose != null -> {
            HorizontalSplitPane(
                modifier = modifier,
                splitPaneState = splitPaneState
            ) {
                first(minSize = 200.dp) {
                    firstColumnCompose()
                }
                second(minSize = 200.dp) {
                    secondColumnCompose()
                }
                splitter {
                    visiblePart {
                        Box(
                            Modifier
                                .width(12.dp)
                                .fillMaxHeight()
                        )
                    }
                    handle {
                        Box(
                            Modifier
                                .markAsHandle()
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                                .width(12.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
        firstColumnCompose != null -> firstColumnCompose()
        secondColumnCompose != null -> secondColumnCompose()
    }
}