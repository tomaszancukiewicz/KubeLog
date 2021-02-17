package com.payu.kube.log.service.namespaces

import com.payu.kube.log.model.PodListState
import com.payu.kube.log.service.pods.PodStoreService
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

@Service
class NamespaceStoreService(
    private val namespaceService: NamespaceService,
    private val podStoreService: PodStoreService
) {
    private val log = logger()

    private final val allNamespaces: ObservableList<String> = FXCollections.observableArrayList()
    final val allNamespacesSorted: ObservableList<String> = allNamespaces.sorted()
    final val currentNamespace: StringProperty = SimpleStringProperty(null)

    private var thread: Thread? = null

    @PostConstruct
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
            Platform.runLater {
                allNamespaces.setAll(namespaces)
            }

            val cNamespace = runCatching { namespaceService.readCurrentNamespace() }
                .getOrNull() ?: return@thread
            Platform.runLater {
                currentNamespace.set(cNamespace)
            }
            thread = null
        }
    }

    @PreDestroy
    fun destroy() {
        thread?.join()
        log.info("Namespace store service destroy")
    }
}