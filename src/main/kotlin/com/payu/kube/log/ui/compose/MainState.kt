package com.payu.kube.log.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.NamespaceService
import com.payu.kube.log.ui.compose.list.PodListState
import com.payu.kube.log.ui.compose.tab.LogTab
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.util.FlowUtils.zipWithNext
import com.payu.kube.log.util.LoadableResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainState(private val coroutineScope: CoroutineScope) {
    val state: MutableStateFlow<LoadableResult<Unit>> = MutableStateFlow(LoadableResult.Loading)
    val currentNamespace = MutableStateFlow<String?>(null)
    val namespaces = MutableStateFlow(listOf<String>())
    var tailLogs by mutableStateOf(true)
        private set
    private var podsListVisible by mutableStateOf(true)

    val logTabsState = LogTabsState()
    val podListState = PodListState(coroutineScope)

    val windowTitle = currentNamespace.map {
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
    val newReadyAppsFlow = podOfOpenAppsFlow
        .zipWithNext { old, new ->
            new.filter { (k, v) ->
                val oldVal = old?.get(k) ?: return@filter false
                oldVal.isReady != v.isReady && v.isReady
            }.values
        }
        .flatMapConcat { it.asFlow() }

    suspend fun loadData() {
        state.value = LoadableResult.Loading

        try {
            namespaces.value = NamespaceService.readAllNamespaceSuspending()
            changeNamespace(NamespaceService.readCurrentNamespaceSuspending())
            state.value = LoadableResult.Value(Unit)
        } catch (e: Exception) {
            state.value = LoadableResult.Error(e)
        }
    }

    fun togglePodListVisible() {
        podsListVisible = !podsListVisible
    }

    fun changeTailLogs(newTailLogs: Boolean) {
        tailLogs = newTailLogs
    }

    fun changeNamespace(newNamespace: String) {
        logTabsState.closeAll()
        currentNamespace.value = newNamespace
        podListState.showNamespace(newNamespace)
    }

    fun reloadNamespace() {
        currentNamespace.value?.let { podListState.showNamespace(it) }
    }

    fun openTab(it: PodInfo) {
        val tab = LogTab(it, tailLogs, coroutineScope, podListState.list)
        logTabsState.open(tab)
    }

    fun isPodListVisible(): Boolean {
        return podsListVisible || logTabsState.tabs.isEmpty()
    }

    fun isTabListVisible(): Boolean {
        return logTabsState.tabs.isNotEmpty()
    }
}