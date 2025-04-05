package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.tab.LogTab
import com.payu.kube.log.ui.compose.tab.content.search.SearchType
import com.payu.kube.log.ui.compose.tab.content.search.SearchView
import com.payu.kube.log.util.Item

@Composable
fun TabContent(logTab: LogTab, onOpenPod: (PodInfo) -> Unit) {
    val podInfo by logTab.podInfoState.collectAsState()
    val newestAppPod by logTab.newestAppPodState.collectAsState()
    val logs by logTab.logs.collectAsState()
    val query by logTab.search.query
    val scrollState = logTab.scrollState

    LaunchedEffect(logs, logTab.settings.autoscroll) {
        logs.lastIndex
            .takeIf { it >= 0 && logTab.settings.autoscroll }
            ?.let { scrollState.scrollToItem(it) }
    }

    LaunchedEffect(query, logTab.search.searchType) {
        val q = query ?: return@LaunchedEffect
        logTab.settings.autoscroll = false

        if (logTab.search.searchType != SearchType.MARK) {
            return@LaunchedEffect
        }

        val indexToScroll = logs.indexOfLast {
            (it as? Item)?.let { item -> q.check(item.value) } ?: false
        }.takeIf { it >= 0 } ?: return@LaunchedEffect
        scrollState.scrollToItem(indexToScroll)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PodInfoView(podInfo, modifier = Modifier.weight(1.0f))
            SettingsView(logTab.settings) { logTab.clear() }
        }
        SearchView(logTab.search) { }
        Box(modifier = Modifier.weight(1.0f)) {
            Lines(
                logs, logTab.settings, logTab.search, scrollState,
                { logTab.showBefore(it) }, { logTab.showAfter(it) }
            )
        }
        newestAppPod?.let {
            NewestPodView(it, onOpenPod)
        }
    }
}


