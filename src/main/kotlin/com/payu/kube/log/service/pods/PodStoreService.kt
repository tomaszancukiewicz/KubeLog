package com.payu.kube.log.service.pods

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodListState
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.fixedRateTimer

@Service
class PodStoreService(private val podService: PodService) : PodListChangeInterface {
    private val log = logger()

    private val pods = MutableStateFlow(listOf<PodInfo>())

    val statePodsSorted = pods.map {
        pods.value.sortedWith(
            compareBy<PodInfo> { it.calculatedAppName }
                .thenBy { it.creationTimestamp }
                .thenBy { it.name }
        )
    }

    val podsSorted: ObservableList<PodInfo> = FXCollections.observableArrayList()

    val stateStatus = MutableStateFlow<PodListState>(PodListState.LoadingPods)
    val status: ObjectProperty<PodListState> = SimpleObjectProperty(PodListState.LoadingPods)

    private var podWatchers = mutableMapOf<String, MutableList<PodChangeInterface>>()
    private var newAppContainerWatchers = mutableMapOf<String, MutableList<PodWithAppInterface>>()
    private var timer: Timer? = null

    @PostConstruct
    fun init() {
        podService.registerOnNewPods(this)

        timer = fixedRateTimer(daemon = true, period = 1000) {
            scheduleRemoveOldPods()
        }
    }

    @PreDestroy
    fun destroy() {
        timer?.cancel()
    }

    fun scheduleRemoveOldPods() {
        val now = Instant.now()
        val podsToDelete = pods.value.filter { it.canBeRemoved(now) }.toList()
        if (podsToDelete.isEmpty()) {
            return
        }
        val newPodsList = pods.value.toMutableList()
        podsToDelete.forEach { pod ->
            podWatchers.remove(pod.name)
            newPodsList.removeIf { it.isSamePod(pod) }
        }
        pods.value = newPodsList
    }

    override fun onWholeList(map: Map<String, PodInfo>) {
        pods.value = map.values.toList()
        map.values.forEach { pod ->
            notifyPodChange(pod)
        }
        map.values
            .groupBy { it.calculatedAppName }
            .forEach { (appName, pods) ->
                val newestPod = pods.maxByOrNull { it.creationTimestamp } ?: return@forEach
                notifyAppChange(appName, newestPod)
            }
    }

    override fun onPodChange(pod: PodInfo) {
        log.info("Pod changed $pod")

        val newList = pods.value.toMutableList()
        val index = newList.indexOfFirst { it.isSamePod(pod) }
        if (index >= 0) {
            newList[index] = pod
        } else if (!pod.canBeRemoved()) {
            newList.add(pod)
        }
        pods.value = newList

        notifyPodChange(pod)
        val newestPod = getNewestPodForApp(pod.calculatedAppName) ?: return
        notifyAppChange(pod.calculatedAppName, newestPod)
    }

    fun podFlow(podInfo: PodInfo): Flow<PodInfo> {
        return pods
            .map { list ->
                list.firstOrNull { it.isSamePod(podInfo) }
            }
            .filterNotNull()
    }

    fun newestPodAppFlow(podInfo: PodInfo, removeSame: Boolean = true): Flow<PodInfo?> {
        val appName = podInfo.calculatedAppName

        return pods
            .map { list ->
                list.filter { it.calculatedAppName == appName }
                .maxByOrNull { it.creationTimestamp }
            }
            .filter {
                if (removeSame && it != null) {
                    !it.isSamePod(podInfo)
                } else {
                    true
                }
            }
    }

    fun getNewestPodForApp(appName: String): PodInfo? {
        return pods.value
            .filter { it.calculatedAppName == appName }
            .maxByOrNull { it.creationTimestamp }
    }

    private fun notifyPodChange(pod: PodInfo) {
        podWatchers[pod.name]?.toList()?.forEach {
            it.onPodChange(pod)
        }
    }

    private fun notifyAppChange(appName: String, newPod: PodInfo) {
        newAppContainerWatchers[appName]?.toList()?.forEach {
            it.onNewPodWithApp(newPod)
        }
    }

    override fun onStateChange(state: PodListState) {
        this.stateStatus.value = state
    }

    fun startWatchPod(pod: PodInfo, watcher: PodChangeInterface) {
        podWatchers.compute(pod.name) { _, list ->
            val nList = list ?: mutableListOf()
            nList.add(watcher)
            nList
        }
    }

    fun stopWatchPod(pod: PodInfo, watcher: PodChangeInterface) {
        podWatchers.compute(pod.name) { _, list ->
            val nList = list ?: mutableListOf()
            nList.remove(watcher)
            nList.takeIf { it.isNotEmpty() }
        }
    }

    fun startWatchApp(appName: String, watcher: PodWithAppInterface) {
        newAppContainerWatchers.compute(appName) { _, list ->
            val nList = list ?: mutableListOf()
            nList.add(watcher)
            nList
        }
    }

    fun stopWatchApp(appName: String, watcher: PodWithAppInterface) {
        newAppContainerWatchers.compute(appName) { _, list ->
            val nList = list ?: mutableListOf()
            nList.remove(watcher)
            nList.takeIf { it.isNotEmpty() }
        }
    }
}