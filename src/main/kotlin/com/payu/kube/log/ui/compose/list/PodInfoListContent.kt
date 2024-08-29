package com.payu.kube.log.ui.compose.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@Composable
fun PodInfoListContent(
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

@Preview
@Composable
private fun PodInfoListContentPreview() {
    ThemeProvider {
        PodInfoListContent(listOf())
    }
}