package com.kube.log.ui.compose.tab

import androidx.compose.runtime.*

class LogTabsState {
    var selection by mutableStateOf(0)
        private set
    var tabs = mutableStateListOf<LogTab>()
        private set

    val openAppsFlow = snapshotFlow { tabs.map { it.podInfo.calculatedAppName }.toSet() }

    val active: LogTab?
        get() = selection.let { tabs.getOrNull(it) }

    fun selectTab(index: Int) {
        selection = index
    }

    fun open(tab: LogTab) {
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