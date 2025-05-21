package com.kube.log.ui.compose.list

import androidx.compose.runtime.*
import com.kube.log.model.PodInfo
import com.kube.log.ui.compose.component.ErrorView
import com.kube.log.ui.compose.component.LoadingView
import com.kube.log.util.LoadableResult

@Composable
fun PodInfoList(
    podListState: PodListState,
    onPodClick: (PodInfo) -> Unit = {},
    onReload: (() -> Unit)? = null,
) {
    val listStatus by podListState.state.collectAsState()

    when (val status = listStatus) {
        is LoadableResult.Loading -> LoadingView()
        is LoadableResult.Error -> ErrorView(
            status.error,
            onReload = onReload
        )
        is LoadableResult.Value -> PodInfoListContent(
            podListState,
            onPodClick
        )
    }
}