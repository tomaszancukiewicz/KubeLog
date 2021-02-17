package com.payu.kube.log.service.logs

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.pods.PodStoreService
import javax.annotation.PreDestroy

@Service
@EnableScheduling
class PodLogService(private val podStoreService: PodStoreService) {
    private val log = logger()

    private var listener: ((PodInfo, List<String>) -> Unit)? = null
    private val watchers = mutableMapOf<PodInfo, PodLogsWatcher>()

    @Scheduled(fixedRate = 300)
    fun notifyNewLogsSchedule() {
        for ((pod, podWatcher) in watchers) {
            val newLogs = podWatcher.getNewLogs()
            if (newLogs.isNotEmpty()) {
                listener?.invoke(pod, newLogs)
            }
        }
    }

    fun registerOnNewLogs(listener: (PodInfo, List<String>) -> Unit) {
        this.listener = listener
    }

    fun startLogging(pod: PodInfo) {
        if (pod in watchers) {
            return
        }
        log.info("start pod monitoring ${pod.name}")
        val watcher = PodLogsWatcher(pod)
        podStoreService.startWatchPod(pod, watcher)
        watcher.start()
        watchers[pod] = watcher
    }

    fun stopLogging(pod: PodInfo) {
        val watcher = watchers[pod] ?: return
        log.info("stop pod monitoring ${pod.name}")
        podStoreService.stopWatchPod(pod, watcher)
        watcher.stop()
        watchers.remove(pod)
    }

    @PreDestroy
    fun destroy() {
        val pods = watchers.keys.toSet()
        for (pod in pods) {
            stopLogging(pod)
        }
        log.info("Pod log service destroy")
    }
}

