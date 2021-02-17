package com.payu.kube.log.service.logs

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.springframework.stereotype.Service
import com.payu.kube.log.model.PodInfo
import javax.annotation.PostConstruct

@Service
class PodLogStoreService(private val podLogService: PodLogService) {
    val podLogs = mutableMapOf<PodInfo, ObservableList<String>>()

    private val openedTabs = mutableMapOf<PodInfo, Int>()

    @PostConstruct
    fun init() {
        podLogService.registerOnNewLogs(this::onNewLogs)
    }

    private fun onNewLogs(pod: PodInfo, newLogs: List<String>) {
        Platform.runLater {
            podLogs[pod]?.addAll(newLogs)
        }
    }

    fun startLogging(pod: PodInfo) {
        openedTabs[pod] = (openedTabs[pod] ?: 0) + 1
        if ((openedTabs[pod] ?: 0) > 1) {
            return
        }
        podLogService.startLogging(pod)
        podLogs[pod] = FXCollections.observableArrayList()
    }

    fun stopLogging(pod: PodInfo) {
        if ((openedTabs[pod] ?: 0) > 0) {
            openedTabs[pod] = (openedTabs[pod] ?: 0) - 1
        }
        if ((openedTabs[pod] ?: 0) > 0) {
            return
        }
        podLogService.stopLogging(pod)
        podLogs.remove(pod)
        openedTabs.remove(pod)
    }
}