package com.payu.kube.log.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodListState
import com.payu.kube.log.service.podService
import com.payu.kube.log.service.podStoreService
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.list.PodInfoList
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.ui.compose.tab.TabsView
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
fun MainContent(currentNamespace: String, podsListVisible: Boolean, logTabsState: LogTabsState) {
    val podListStatus by podStoreService.stateStatus.collectAsState()

    LaunchedEffect(currentNamespace) {
        podService.startMonitorNamespace(currentNamespace)
    }

    Box {
        HorizontalSplitPane(splitPaneState = rememberSplitPaneState(0.2f)) {
            if (podsListVisible || logTabsState.logTabs.isEmpty()) {
                first(minSize = 100.dp) {
                    when (val status = podListStatus) {
                        PodListState.LoadingPods -> LoadingView()
                        is PodListState.ErrorPods -> ErrorView(
                            status.message ?: "",
                            onReload = { podService.startMonitorNamespace(currentNamespace) }
                        )
                        PodListState.Data -> PodInfoList { logTabsState.open(it) }
                    }
                }
            }
            if (logTabsState.logTabs.isNotEmpty()) {
                second(minSize = 100.dp) {
                    TabsView(logTabsState)
                }
            }
        }
    }
}