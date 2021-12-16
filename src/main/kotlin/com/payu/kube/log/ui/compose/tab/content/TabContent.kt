package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.tab.LogTab
import com.payu.kube.log.ui.compose.tab.SearchType
import com.payu.kube.log.ui.tab.list.Item
import kotlinx.coroutines.flow.map

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun TabContent(logTab: LogTab, openPod: (PodInfo) -> Unit) {
    val podInfo by logTab.podInfoState.collectAsState()
    val newestAppPod by logTab.newestAppPodState
        .map { pod -> pod?.takeIf { !it.isSamePod(podInfo) } }
        .collectAsState(null)
    val logs by logTab.logs.collectAsState()
    val searchType by logTab.search.searchType
    val query by logTab.search.query
    var autoScroll by logTab.settings.autoscroll
    val scrollState = rememberLazyListState()

    LaunchedEffect(logs, autoScroll) {
        logs.lastIndex
            .takeIf { it >= 0 && autoScroll }
            ?.let { scrollState.scrollToItem(it) }
    }

    LaunchedEffect(query, searchType) {
        val q = query
        if (searchType != SearchType.MARK || q == null) {
            return@LaunchedEffect
        }
        autoScroll = false

        val indexToScroll = logs.indexOfLast {
            (it as? Item)?.let { item -> q.check(item.value) } ?: false
        }.takeIf { it >= 0 } ?: return@LaunchedEffect
        scrollState.scrollToItem(indexToScroll)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PodInfoView(podInfo, settingsState = logTab.settings, onClear = {
            logTab.clear()
        })
        SearchView(logTab.search) { }
        Box(modifier = Modifier.weight(1.0f)) {
            Lines(
                logs, logTab.settings, logTab.search, scrollState,
                { logTab.showBefore(it) }, { logTab.showAfter(it) }
            )
        }
        newestAppPod?.let {
            NewestPodView(it, openPod)
        }
    }
}


