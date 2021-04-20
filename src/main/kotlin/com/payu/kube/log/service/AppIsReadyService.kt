package com.payu.kube.log.service

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.pods.PodWithAppInterface
import org.springframework.stereotype.Service

@Service
class AppIsReadyService(
    private val notificationService: NotificationService,
    private val podStoreService: PodStoreService
) : PodWithAppInterface {

    private val monitoredApp = mutableMapOf<String, PodInfo?>()
    private val openAppsCounter = mutableMapOf<String, Int>()

    override fun onNewPodWithApp(pod: PodInfo) {
        val oldValue = monitoredApp[pod.calculatedAppName]
        monitoredApp[pod.calculatedAppName] = pod
        if (oldValue?.isReady != pod.isReady && pod.isReady) {
            showNotification(pod)
        }
    }

    private fun showNotification(pod: PodInfo) {
        notificationService.showNotification(
            "KubeLog - ${pod.calculatedAppName}",
            "${pod.name} is ready")
    }

    fun startMonitorChangesForApp(appName: String) {
        val openAppsCount = openAppsCounter.compute(appName) { _, value ->
            (value ?: 0) + 1
        } ?: 0
        if (openAppsCount > 1) return
        if (appName in monitoredApp) return
        podStoreService.getNewestPodForApp(appName)?.let {
            monitoredApp[it.calculatedAppName] = it
        }
        podStoreService.startWatchApp(appName, this)
    }

    fun stopMonitorChangesForApp(appName: String) {
        val openAppsCount = openAppsCounter.compute(appName) { _, value ->
            (value ?: 1) - 1
        } ?: 0
        if (openAppsCount > 0) return
        if (appName !in monitoredApp) return
        podStoreService.stopWatchApp(appName, this)
        monitoredApp.remove(appName)
        openAppsCounter.remove(appName)
    }
}