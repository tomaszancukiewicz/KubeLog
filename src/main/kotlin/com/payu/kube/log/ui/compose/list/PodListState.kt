package com.payu.kube.log.ui.compose.list

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.PodService
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.util.LoadableResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PodListState(private val coroutineScope: CoroutineScope) {
    private var monitorJob: Job? = null
    val state = MutableStateFlow<LoadableResult<Unit>>(LoadableResult.Loading)
    val list = MutableStateFlow<List<PodInfo>>(listOf())

    val filterText = MutableStateFlow("")
    val filteredList = list.combine(filterText) { l, t ->
        val filterPredicate: (PodInfo) -> Boolean =
            t.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { SearchQueryCompilerService.compile(it) }
                ?.let { { pod -> it.check(pod.name) } }
                ?: { true }
        l.filter(filterPredicate)
    }.stateIn(coroutineScope, SharingStarted.Lazily, listOf())

    fun showNamespace(namespace: String) {
        monitorJob?.cancel()
        monitorJob = coroutineScope.launch {
            monitorNamespace(namespace)
        }
    }

    private suspend fun monitorNamespace(namespace: String) {
        PodService.monitorPods(namespace)
            .map { it.sortedWith(PodInfo.COMPARATOR) }
            .onEach {
                state.value = LoadableResult.Value(Unit)
                list.value = it
            }
            .catch {
                list.value = listOf()
                state.value = LoadableResult.Error(it)
            }
            .onStart {
                list.value = listOf()
                state.value = LoadableResult.Loading
            }.collect()
    }

    fun changeFilterText(it: String) {
        filterText.value = it
    }
}