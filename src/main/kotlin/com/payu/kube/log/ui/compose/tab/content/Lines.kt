package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.payu.kube.log.util.VirtualItem

@Composable
fun Lines(
    logs: List<VirtualItem<String>>, settings: SettingsState, search: SearchState, scrollState: LazyListState,
    onPrevClick: (Int) -> Unit, onAfterClick: (Int) -> Unit
) {
    val query by search.query
    val stateHorizontal = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        SelectionContainer {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (!settings.isWrap) it.horizontalScroll(stateHorizontal) else it }
            ) {
                LazyColumn(state = scrollState) {
                    items(logs.size) { index ->
                        Line(logs[index], query,
                            { onPrevClick(index) }, { onAfterClick(index) },
                            modifier = Modifier.fillMaxWidth()
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