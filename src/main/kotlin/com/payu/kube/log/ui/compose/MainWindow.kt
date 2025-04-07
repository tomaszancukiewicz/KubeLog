package com.payu.kube.log.ui.compose

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.SnackbarState
import com.payu.kube.log.ui.compose.component.shortcut.*
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider
import com.payu.kube.log.ui.compose.menu.NamespacesMenu
import com.payu.kube.log.ui.compose.menu.TailLogsMenu
import com.payu.kube.log.ui.compose.update.UpdateDialog
import com.payu.kube.log.util.LoadableResult

@Composable
fun MainWindow(exitApplication: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = SnackbarState.current
    val mainState = remember { MainState(coroutineScope) }

    LaunchedEffect(Unit) {
        mainState.loadData()
    }

    val state by mainState.state.collectAsState()
    val title by mainState.windowTitle.collectAsState()
    val tailLogs by mainState.tailLogs.collectAsState()

    Window(
        onCloseRequest = exitApplication,
        state = rememberWindowState(size = DpSize(900.dp, 600.dp)),
        title = title,
        icon = painterResource("AppIcon.png"),
        onPreviewKeyEvent = {
            if (it.type != KeyEventType.KeyDown) {
                return@Window false
            }
            when {
                TOGGLE_PODS_SHORTCUT.match(it) -> {
                    mainState.togglePodListVisible()
                    true
                }
                TOGGLE_SEARCH_SHORTCUT.match(it) -> {
                    mainState.logTabsState.active?.search?.toggleVisible()
                    true
                }
                CLOSE_ALL_TABS_SHORTCUT.match(it) -> {
                    mainState.logTabsState.closeAll()
                    true
                }
                CLOSE_TAB_SHORTCUT.match(it) -> {
                    mainState.logTabsState.active?.let { active -> mainState.logTabsState.close(active) }
                    true
                }
                CLEAR_LOGS_SHORTCUT.match(it) -> {
                    mainState.logTabsState.active?.clear()
                    true
                }
                else -> false
            }
        }
    ) {
        MenuBar {
            NamespacesMenu(
                mainState.namespacesState
            )
            TailLogsMenu(
                tailLogs,
                onChangeTail = mainState::changeTailLogs
            )
        }
        ThemeProvider {
            Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) {
                UpdateDialog()
                when (val s = state) {
                    is LoadableResult.Loading -> LoadingView()
                    is LoadableResult.Error -> ErrorView(s.error)
                    is LoadableResult.Value -> MainContent(mainState)
                }
            }
        }
    }
}
