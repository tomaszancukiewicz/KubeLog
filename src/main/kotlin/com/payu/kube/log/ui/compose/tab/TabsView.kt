package com.payu.kube.log.ui.compose.tab

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ScrollableTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.tab.content.TabContent

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun TabsView(logTabsState: LogTabsState, openPod: (PodInfo) -> Unit) {
    Column {
        ScrollableTabRow(selectedTabIndex = logTabsState.selection) {
            logTabsState.tabs.forEachIndexed { index, logTab ->
                CustomTab(
                    podInfoState = logTab.podInfoState,
                    selected = logTabsState.selection == index,
                    onClick = { logTabsState.selection = index },
                    onClose = { logTabsState.close(logTab) }
                )
            }
        }
        logTabsState.active?.let {
            TabContent(it, openPod)
        }
    }
}