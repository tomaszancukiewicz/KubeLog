package com.payu.kube.log.ui.compose.list

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.component.TextField

@Composable
fun PodInfoListContent(
    podListState: PodListState,
    onPodClick: (PodInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredList by podListState.filteredList.collectAsState()
    val filterText by podListState.filterText.collectAsState()

    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth()
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                        podListState.changeFilterText("")
                        true
                    } else {
                        false
                    }
                },
            placeholder = "Search pod",
            value = filterText,
            onValueChange = { podListState.changeFilterText(it) },
        )
        Spacer(Modifier.height(8.dp))
        PodList(filteredList, onPodClick)
    }
}