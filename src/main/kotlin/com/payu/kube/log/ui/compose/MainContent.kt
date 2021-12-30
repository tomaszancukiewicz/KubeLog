package com.payu.kube.log.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.pods.PodService
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.NotificationCenter
import com.payu.kube.log.ui.compose.list.PodInfoList
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.ui.compose.tab.TabsView
import com.payu.kube.log.util.FlowUtils.zipWithNext
import com.payu.kube.log.util.LoadableResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
fun MainContent(currentNamespace: String, podsListVisible: Boolean, logTabsState: LogTabsState) {
    val coroutineScope = rememberCoroutineScope()
    val notificationCenter = NotificationCenter.current
    val currentNamespaceChannel = remember { MutableSharedFlow<String>(1) }
    val podListDataStateFlow: StateFlow<LoadableResult<List<PodInfo>>> = remember {
        currentNamespaceChannel
            .flatMapLatest { currentNamespace ->
                PodService.monitorPods(currentNamespace)
                    .map { it.sortedWith(PodInfo.COMPARATOR) }
                    .map<List<PodInfo>, LoadableResult<List<PodInfo>>> { LoadableResult.Value(it) }
                    .catch { LoadableResult.Error(it) }
                    .onStart { emit(LoadableResult.Loading) }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, LoadableResult.Loading)
    }
    val podListStateFlow: Flow<List<PodInfo>> = remember {
        podListDataStateFlow
            .map { (it as? LoadableResult.Value)?.value ?: listOf() }
    }
    val podListData by podListDataStateFlow.collectAsState()

    LaunchedEffect(currentNamespace) {
        currentNamespaceChannel.tryEmit(currentNamespace)
    }

    LaunchedEffect(Unit) {
        val podOfOpenAppsFlow = podListStateFlow
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

        newReadyAppsFlow
            .flatMapConcat { it.asFlow() }
            .onEach {
                val notification = Notification(
                    "KubeLog - ${it.calculatedAppName}",
                    "${it.name} is ready", Notification.Type.Info
                )
                notificationCenter.sendNotification(notification)
            }
            .launchIn(coroutineScope)
    }

    HorizontalSplitPane(splitPaneState = rememberSplitPaneState(0.2f)) {
        if (podsListVisible || logTabsState.tabs.isEmpty()) {
            first(minSize = 400.dp) {
                when (val status = podListData) {
                    is LoadableResult.Loading -> LoadingView()
                    is LoadableResult.Error -> ErrorView(
                        status.error.message ?: "",
                        onReload = {
                            currentNamespaceChannel.tryEmit(currentNamespace)
                        }
                    )
                    is LoadableResult.Value -> PodInfoList(status.value) { logTabsState.open(it, podListStateFlow) }
                }
            }
        }
        if (logTabsState.tabs.isNotEmpty()) {
            second(minSize = 100.dp) {
                TabsView(logTabsState) { logTabsState.open(it, podListStateFlow) }
            }
        }
    }
}