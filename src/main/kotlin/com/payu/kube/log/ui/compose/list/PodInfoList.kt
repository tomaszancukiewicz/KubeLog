package com.payu.kube.log.ui.compose.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PodInfoList(
    podList: List<PodInfo>,
    searchTextState: MutableState<String> =  remember { mutableStateOf("") },
    onPodClick: (PodInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredPodList by remember(podList, searchTextState) {
        derivedStateOf {
            val filterPredicate: (PodInfo) -> Boolean =
                searchTextState.value.trim()
                    .takeIf { it.isNotEmpty() }
                    ?.let { SearchQueryCompilerService.compile(it) }
                    ?.let { { pod -> it.check(pod.name) } }
                    ?: { true }
            podList.filter(filterPredicate)
        }
    }

    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth()
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                        searchTextState.value = ""
                        true
                    } else {
                        false
                    }
                },
            placeholder = "Search pod",
            value = searchTextState.value,
            onValueChange = { searchTextState.value = it },
        )
        Spacer(Modifier.height(8.dp))
        PodList(filteredPodList, onPodClick)
    }
}

@Composable
fun PodList(podList: List<PodInfo>, onPodClick: (PodInfo) -> Unit) {
    val scrollState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(state = scrollState) {
            items(podList) { item ->
                PodInfoViewCell(item, onPodClick)
                Spacer(Modifier.height(8.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = scrollState
            )
        )
    }
}

@Preview
@Composable
private fun PodInfoListPreview() {
    ThemeProvider {
        PodInfoList(listOf())
    }
}