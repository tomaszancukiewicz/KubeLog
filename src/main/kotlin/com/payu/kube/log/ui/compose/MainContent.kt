package com.payu.kube.log.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import com.payu.kube.log.model.PodListState
import com.payu.kube.log.service.podService
import com.payu.kube.log.service.podStoreService
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.NotificationCenter
import com.payu.kube.log.ui.compose.list.PodInfoList
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.ui.compose.tab.TabsView
import com.payu.kube.log.util.FlowUtils.zipWithNext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@FlowPreview
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
fun MainContent(currentNamespace: String, podsListVisible: Boolean, logTabsState: LogTabsState) {
    val coroutineScope = rememberCoroutineScope()
    val notificationCenter = NotificationCenter.current
    val podListStatus by podStoreService.stateStatus.collectAsState()

    LaunchedEffect(currentNamespace) {
        podService.startMonitorNamespace(currentNamespace)
    }

    LaunchedEffect(Unit) {
        val podOfOpenAppsFlow = podStoreService.statePodsSorted
            .combine(logTabsState.openAppsFlow)  { list, monitoredApps ->
                list
                    .filter { it.calculatedAppName in monitoredApps }
                    .sortedBy { it.creationTimestamp }
                    .associateBy { it.calculatedAppName }
            }

        val newReadyAppsFlow = podOfOpenAppsFlow
            .zipWithNext { old, new ->
                new.filter { (k, v) ->
                    val oldVal = old?.get(k) ?: return@filter false
                    oldVal.isReady != v.isReady && v.isReady
                }.values
            }

        newReadyAppsFlow
            .flatMapConcat { it.asFlow() }
            .onEach {
                val notification =  Notification(
                    "KubeLog - ${it.calculatedAppName}",
                    "${it.name} is ready", Notification.Type.Info
                )
                notificationCenter.sendNotification(notification)
            }
            .launchIn(coroutineScope)
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