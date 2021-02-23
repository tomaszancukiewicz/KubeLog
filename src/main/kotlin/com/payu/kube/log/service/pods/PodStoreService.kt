package com.payu.kube.log.service.pods

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodListState
import java.time.Instant
import javax.annotation.PostConstruct

@Service
class PodStoreService(private val podService: PodService) : PodListChangeInterface {
    private val log = logger()

    private final val pods: ObservableList<PodInfo> = FXCollections.observableArrayList()
    final val podsSorted: ObservableList<PodInfo> = pods.sorted(
        compareBy<PodInfo> { it.calculatedAppName }
            .thenBy { it.creationTimestamp }
            .thenBy { it.name }
    )
    final val status: ObjectProperty<PodListState> = SimpleObjectProperty(PodListState.LoadingPods)

    private var podWatchers = mutableMapOf<PodInfo, MutableList<PodChangeInterface>>()

    @PostConstruct
    fun init() {
        podService.registerOnNewPods(this)
    }

    @Scheduled(fixedRate = 1000)
    fun scheduleRemoveOldPods() {
        val now = Instant.now()
        val podsToDelete = pods.filter { it.canBeRemoved(now) }.toList()
        Platform.runLater {
            podsToDelete.forEach { pod ->
                podWatchers.remove(pod)
                pods.removeIf { it == pod }
            }
        }
    }

    override fun onWholeList(map: Map<String, PodInfo>) {
        Platform.runLater {
            pods.setAll(map.values)
            map.values.forEach { pod ->
                notifyPodChange(pod)
            }
        }
    }

    override fun onPodChange(pod: PodInfo) {
        Platform.runLater {
            log.info("Pod changed $pod")
            val index = pods.indexOf(pod)
            if (index >= 0) {
                pods[index] = pod
            } else if (!pod.canBeRemoved()) {
                pods.add(pod)
            }
            notifyPodChange(pod)
        }
    }

    private fun notifyPodChange(pod: PodInfo) {
        podWatchers[pod]?.toList()?.forEach {
            it.onPodChange(pod)
        }
    }

    override fun onStateChange(state: PodListState) {
        Platform.runLater {
            this.status.set(state)
        }
    }

    fun startWatchPod(pod: PodInfo, watcher: PodChangeInterface) {
        podWatchers.compute(pod) { _, list ->
            val nList = list ?: mutableListOf()
            nList.add(watcher)
            nList
        }
    }

    fun stopWatchPod(pod: PodInfo, watcher: PodChangeInterface) {
        podWatchers.compute(pod) { _, list ->
            val nList = list ?: mutableListOf()
            nList.remove(watcher)
            nList.takeIf { it.isNotEmpty() }
        }
    }
}