package com.payu.kube.log.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Notification
import com.payu.kube.log.ui.compose.component.MyHorizontalSplitPane
import com.payu.kube.log.ui.compose.component.NotificationCenter
import com.payu.kube.log.ui.compose.component.SnackbarState
import com.payu.kube.log.ui.compose.list.PodInfoList
import com.payu.kube.log.ui.compose.list.PodListState
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.ui.compose.tab.TabsView
import com.payu.kube.log.util.FlowUtils.zipWithNext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSplitPaneApi::class)
@Composable
fun MainContent(currentNamespace: String, tailLogs: Boolean, podsListVisible: Boolean, logTabsState: LogTabsState) {
    val coroutineScope = rememberCoroutineScope()
    val notificationCenter = NotificationCenter.current
    val snackbarState = SnackbarState.current
    val podListState = remember { PodListState(coroutineScope) }

    LaunchedEffect(currentNamespace) {
        podListState.showNamespace(currentNamespace)
    }

    LaunchedEffect(Unit) {
        val podOfOpenAppsFlow = podListState.list
            .combine(logTabsState.openAppsFlow) { list, monitoredApps ->
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
            .flatMapConcat { it.asFlow() }

        newReadyAppsFlow
            .onEach {
                val notification = Notification(
                    "KubeLog - ${it.calculatedAppName}",
                    "${it.name} is ready", Notification.Type.Info
                )
                notificationCenter.sendNotification(notification)
                snackbarState.showSnackbar("${it.name} is ready", withDismissAction = true)
            }
            .launchIn(coroutineScope)
    }

    val firstColumnCompose = @Composable {
        PodInfoList(
            podListState,
            onPodClick = {
                logTabsState.open(
                    it,
                    tailLogs,
                    podListState.list
                )
            },
            onReload = {
                podListState.showNamespace(currentNamespace)
            }
        )
    }

    val secondColumnCompose = @Composable {
        TabsView(
            logTabsState,
            { logTabsState.open(it, tailLogs, podListState.list) }
        )
    }

    MyHorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(0.2f),
        firstColumnCompose = firstColumnCompose
            .takeIf { podsListVisible || logTabsState.tabs.isEmpty() },
        secondColumnCompose = secondColumnCompose
            .takeIf { logTabsState.tabs.isNotEmpty() }
    )
}