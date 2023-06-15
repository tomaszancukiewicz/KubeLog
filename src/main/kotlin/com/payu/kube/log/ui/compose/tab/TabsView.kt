package com.payu.kube.log.ui.compose.tab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.Composable
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.tab.content.TabContent

@Composable
fun TabsView(logTabsState: LogTabsState, openPod: (PodInfo) -> Unit) {
    Column {
        ScrollableTabRow(
            selectedTabIndex = logTabsState.selection,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
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