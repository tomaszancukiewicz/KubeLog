package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.payu.kube.log.ui.compose.tab.SearchState
import com.payu.kube.log.ui.compose.tab.SettingsState
import com.payu.kube.log.ui.tab.list.VirtualItem

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun Lines(
    logs: List<VirtualItem<String>>, settings: SettingsState, search: SearchState, scrollState: LazyListState,
    onPrevClick: (Int) -> Unit, onAfterClick: (Int) -> Unit
) {
    val query by search.query
    val isWrap by settings.isWrap
    val stateHorizontal = rememberScrollState()

    SelectionContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (!isWrap) it.horizontalScroll(stateHorizontal) else it }
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
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
            if (!isWrap) {
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    adapter = rememberScrollbarAdapter(stateHorizontal)
                )
            }
        }
    }
}