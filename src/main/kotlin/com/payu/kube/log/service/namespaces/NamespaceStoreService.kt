package com.payu.kube.log.service.namespaces

import com.payu.kube.log.model.PodListState
import com.payu.kube.log.service.pods.PodStoreService
import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

@Service
class NamespaceStoreService(
    private val namespaceService: NamespaceService,
    private val podStoreService: PodStoreService
) {
    private val log = logger()

    private val stateAllNamespace = MutableStateFlow<List<String>>(listOf())
    private val allNamespaces: ObservableList<String> = FXCollections.observableArrayList()

    val stateAllNamespacesSorted = stateAllNamespace.map {
        it.sorted()
    }
    val allNamespacesSorted: ObservableList<String> = allNamespaces.sorted()

    val stateCurrentNamespace = MutableStateFlow<String?>(null)
    val currentNamespace: StringProperty = SimpleStringProperty(null)

    private var thread: Thread? = null

    fun init() {
        if (thread != null) {
            return
        }
        thread = thread(isDaemon = true, name = "namespace-thread") {
            podStoreService.onStateChange(PodListState.LoadingPods)
            val namespaces = runCatching { namespaceService.readAllNamespace() }
                .onFailure {
                    podStoreService.onStateChange(PodListState.ErrorPods(it.message))
                }.getOrNull() ?: return@thread
            stateAllNamespace.value = namespaces

            val cNamespace = runCatching { namespaceService.readCurrentNamespace() }
                .getOrNull() ?: return@thread
            stateCurrentNamespace.value = cNamespace
            thread = null
        }
    }

    @PreDestroy
    fun destroy() {
        thread?.join()
        log.info("Namespace store service destroy")
    }
}