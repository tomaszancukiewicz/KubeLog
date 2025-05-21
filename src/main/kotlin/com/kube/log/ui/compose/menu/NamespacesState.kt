package com.kube.log.ui.compose.menu

import com.kube.log.service.NamespaceService
import com.kube.log.util.LoadableResult
import kotlinx.coroutines.flow.MutableStateFlow

class NamespacesState {
    val state = MutableStateFlow<LoadableResult<Unit>>(LoadableResult.Loading)
    val namespaces = MutableStateFlow(listOf<String>())
    val currentNamespace = MutableStateFlow<String?>(null)

    fun changeNamespace(namespace: String) {
        currentNamespace.value = namespace
    }

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
}