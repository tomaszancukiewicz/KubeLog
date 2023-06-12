package com.payu.kube.log.ui.compose.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.compose.component.ThemeProvider
import com.payu.kube.log.ui.compose.component.TextField

@ExperimentalComposeUiApi
@Composable
fun PodInfoList(podList: List<PodInfo>,
                searchTextState: MutableState<String> =  remember { mutableStateOf("") },
                onPodClick: (PodInfo) -> Unit) {
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

    Column {
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
                Divider()
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
@ExperimentalComposeUiApi
@Composable
private fun PodInfoListPreview() {
    ThemeProvider {
        PodInfoList(listOf()) {}
    }
}