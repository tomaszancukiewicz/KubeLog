package com.payu.kube.log.ui.compose.list

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            value = filterText,
            onValueChange = { podListState.changeFilterText(it) },
            modifier = Modifier.fillMaxWidth()
                .onKeyEvent {
                    if (it.type != KeyEventType.KeyDown) {
                        return@onKeyEvent false
                    }
                    when (it.key) {
                        Key.Escape -> {
                            podListState.changeFilterText("")
                            true
                        }

                        else -> false
                    }
                },
            placeholder = "Search pod",
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    null,
                    Modifier.size(20.dp)
                )
            },
        )
        Spacer(Modifier.height(8.dp))
        PodList(filteredList, onPodClick)
    }
}