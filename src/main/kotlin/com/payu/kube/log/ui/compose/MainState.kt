package com.payu.kube.log.ui.compose

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.list.PodListState
import com.payu.kube.log.ui.compose.menu.NamespacesState
import com.payu.kube.log.ui.compose.tab.LogTab
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.util.FlowUtils.zipWithNext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainState(private val coroutineScope: CoroutineScope) {
    val tailLogs = MutableStateFlow(true)
    val podListVisible = MutableStateFlow(true)
    val namespacesState = NamespacesState()
    val logTabsState = LogTabsState()
    val podListState = PodListState(coroutineScope)

    val state = namespacesState.state
    val windowTitle = namespacesState.currentNamespace.map {
        if (!it.isNullOrEmpty()) {
            "KubeLog - $it"
        } else {
            "Kubelog"
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, "KubeLog")

    private val podOfOpenAppsFlow = podListState.list
        .combine(logTabsState.openAppsFlow) { list, monitoredApps ->
            list
                .filter { it.calculatedAppName in monitoredApps }
                .sortedBy { it.creationTimestamp }
                .associateBy { it.calculatedAppName }
        }
    val newReadyApps = podOfOpenAppsFlow
        .zipWithNext { old, new ->
            new.filter { (k, v) ->
                val oldVal = old?.get(k) ?: return@filter false
                oldVal.isReady != v.isReady && v.isReady
            }.values
        }
        .flatMapConcat { it.asFlow() }

    suspend fun loadData() = coroutineScope {
        val watchingJob = async {
            namespacesState.currentNamespace
                .filterNotNull()
                .collect(::monitorNamespace)
        }
        namespacesState.loadData()
        watchingJob.await()
    }

    private fun monitorNamespace(newNamespace: String) {
        logTabsState.closeAll()
        podListState.showNamespace(newNamespace)
    }

    fun reloadNamespace() {
        namespacesState.currentNamespace.value
            ?.let { podListState.showNamespace(it) }
    }

    fun openTab(it: PodInfo) {
        val tab = LogTab(it, tailLogs.value, coroutineScope, podListState.list)
        logTabsState.open(tab)
    }

    fun togglePodListVisible() {
        podListVisible.update { !it }
    }

    fun changeTailLogs(newTailLogs: Boolean) {
        tailLogs.value = newTailLogs
    }
}