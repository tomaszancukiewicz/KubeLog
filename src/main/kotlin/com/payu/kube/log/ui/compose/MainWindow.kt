package com.payu.kube.log.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.SnackbarState
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
                it.isMetaPressed && it.key == Key.T -> {
                    mainState.togglePodListVisible()
                    true
                }
                it.isMetaPressed && it.key == Key.F -> {
                    mainState.logTabsState.active?.search?.toggleVisible()
                    true
                }
                it.isMetaPressed && it.isShiftPressed && it.key == Key.W -> {
                    mainState.logTabsState.closeAll()
                    true
                }
                it.isMetaPressed && it.key == Key.W -> {
                    mainState.logTabsState.active?.let { active -> mainState.logTabsState.close(active) }
                    true
                }
                it.isMetaPressed && it.isShiftPressed && it.key == Key.C -> {
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
                mainState.tailLogs,
                onChangeTail = mainState::changeTailLogs
            )
        }
        ThemeProvider {
            Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) {
                UpdateDialog()
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                ) {
                    when (val s = state) {
                        is LoadableResult.Loading -> LoadingView()
                        is LoadableResult.Error -> ErrorView(s.error.message ?: "")
                        is LoadableResult.Value -> MainContent(mainState)
                    }
                }
            }
        }
    }
}
