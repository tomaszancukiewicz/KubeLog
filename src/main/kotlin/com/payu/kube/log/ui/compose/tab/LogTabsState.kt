package com.payu.kube.log.ui.compose.tab

import androidx.compose.runtime.*
import com.payu.kube.log.model.PodInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class LogTabsState(private val coroutineScope: CoroutineScope) {
    var selection by mutableStateOf(0)
    var tabs = mutableStateListOf<LogTab>()
        private set

    val openAppsFlow = snapshotFlow { tabs.map { it.podInfo.calculatedAppName }.toSet() }

    val active: LogTab?
        get() = selection.let { tabs.getOrNull(it) }

    fun open(podInfo: PodInfo, tailLogs: Boolean, allPodsFlow: Flow<List<PodInfo>>) {
        val tab = LogTab(podInfo, tailLogs, coroutineScope, allPodsFlow)
        tab.init()
        tabs.add(tab)
        selection = tabs.lastIndex
    }

    fun close(logTab: LogTab) {
        logTab.destroy()
        tabs.remove(logTab)
        selection = selection.coerceAtMost(tabs.lastIndex)
    }

    fun closeAll() {
        tabs.forEach { it.destroy() }
        tabs.clear()
        selection = 0
    }
}