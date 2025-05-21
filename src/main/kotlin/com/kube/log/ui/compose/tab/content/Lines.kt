package com.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.kube.log.ui.compose.tab.content.search.SearchState
import com.kube.log.util.VirtualItem

@Composable
fun Lines(
    logs: List<VirtualItem<String>>, settings: SettingsState, search: SearchState, scrollState: LazyListState,
    onPrevClick: (Int) -> Unit, onAfterClick: (Int) -> Unit
) {
    val stateHorizontal = rememberScrollState()

    Card(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxSize()
                        .let { if (!settings.isWrap) it.horizontalScroll(stateHorizontal) else it }
                ) {
                    var lazyColumnSize by remember { mutableStateOf(Size.Zero) }

                    LazyColumn(
                        modifier = Modifier.onSizeChanged { lazyColumnSize = it.toSize() },
                        state = scrollState,
                    ) {
                        items(logs.size) { index ->
                            Line(logs[index], search.query.value,
                                { onPrevClick(index) }, { onAfterClick(index) },
                                modifier = Modifier
                                    .widthIn(min = with(LocalDensity.current) { lazyColumnSize.width.toDp() })
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
            if (!settings.isWrap) {
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    adapter = rememberScrollbarAdapter(stateHorizontal)
                )
            }
        }
    }
}