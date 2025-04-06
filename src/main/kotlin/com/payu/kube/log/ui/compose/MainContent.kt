package com.payu.kube.log.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import com.payu.kube.log.ui.compose.component.MyHorizontalSplitPane
import com.payu.kube.log.ui.compose.component.NotificationCenter
import com.payu.kube.log.ui.compose.component.SnackbarState
import com.payu.kube.log.ui.compose.list.PodInfoList
import com.payu.kube.log.ui.compose.tab.TabsView
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun MainContent(mainState: MainState) {
    val coroutineScope = rememberCoroutineScope()
    val notificationCenter = NotificationCenter.current
    val snackbarState = SnackbarState.current
    val listVisible by mainState.podListVisible.collectAsState()
    val tabListVisible = mainState.logTabsState.tabs.isNotEmpty()
    val podListVisible = listVisible || mainState.logTabsState.tabs.isEmpty()

    LaunchedEffect(Unit) {
        mainState.newReadyApps
            .onEach {
                val notification = Notification(
                    "KubeLog - ${it.calculatedAppName}",
                    "${it.name} is ready", Notification.Type.Info
                )
                notificationCenter.sendNotification(notification)
                snackbarState.showSnackbar("${it.name} is ready", withDismissAction = true)
            }
            .launchIn(coroutineScope)
    }

    val firstColumnCompose = @Composable {
        PodInfoList(
            mainState.podListState,
            onPodClick = mainState::openTab,
            onReload = mainState::reloadNamespace
        )
    }

    val secondColumnCompose = @Composable {
        TabsView(
            mainState.logTabsState,
            onOpenPod = mainState::openTab
        )
    }

    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        MyHorizontalSplitPane(
            splitPaneState = rememberSplitPaneState(0.2f),
            firstColumnCompose = firstColumnCompose
                .takeIf { podListVisible },
            secondColumnCompose = secondColumnCompose
                .takeIf { tabListVisible }
        )
    }
}