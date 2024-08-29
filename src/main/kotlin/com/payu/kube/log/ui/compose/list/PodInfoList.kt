package com.payu.kube.log.ui.compose.list

import androidx.compose.runtime.*
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.util.LoadableResult
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PodInfoList(
    podListDataStateFlow: StateFlow<LoadableResult<List<PodInfo>>>,
    searchTextState: MutableState<String> = remember { mutableStateOf("") },
    onPodClick: (PodInfo) -> Unit = {},
    onReload: (() -> Unit)? = null,
) {
    val podListData by podListDataStateFlow.collectAsState()

    when (val status = podListData) {
        is LoadableResult.Loading -> LoadingView()
        is LoadableResult.Error -> ErrorView(
            status.error.message ?: "",
            onReload = onReload
        )
        is LoadableResult.Value -> PodInfoListContent(
            status.value,
            searchTextState,
            onPodClick
        )
    }
}