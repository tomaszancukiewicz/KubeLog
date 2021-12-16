package com.payu.kube.log.ui.compose.list

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.podStoreService
import com.payu.kube.log.service.searchQueryCompilerService

@ExperimentalComposeUiApi
@Composable
fun PodInfoList(onPodClick: (PodInfo) -> Unit) {
    val podList by podStoreService.statePodsSorted.collectAsState(listOf())
    var searchText by remember { mutableStateOf("") }
    val filteredPodList by remember {
        derivedStateOf {
            val filterPredicate: (PodInfo) -> Boolean =
                searchText.trim()
                    .takeIf { it.isNotEmpty() }
                    ?.let { searchQueryCompilerService.compile(it) }
                    ?.let { { pod -> it.check(pod.name) } }
                    ?: { true }
            podList.filter(filterPredicate)
        }
    }

    Column {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth().onKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                    searchText = ""
                    true
                } else {
                    false
                }
            },
            singleLine = true,
            label = { Text("Search pod") },
            shape = CutCornerShape(0.dp),
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