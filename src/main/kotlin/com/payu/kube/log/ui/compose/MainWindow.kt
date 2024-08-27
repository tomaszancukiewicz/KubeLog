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
import com.payu.kube.log.service.NamespaceService
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.SnackbarState
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider
import com.payu.kube.log.ui.compose.menu.NamespacesMenu
import com.payu.kube.log.ui.compose.menu.TailLogsMenu
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.util.LoadableResult

@Composable
fun MainWindow(exitApplication: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = SnackbarState.current
    var tailLogs by remember { mutableStateOf(true) }
    var podsListVisible by remember { mutableStateOf(true) }
    var currentNamespace by remember { mutableStateOf<String?>(null) }
    var namespaces by remember { mutableStateOf(listOf<String>()) }
    val logTabsState = remember { LogTabsState(coroutineScope) }

    val windowTitle by remember {
        derivedStateOf {
            if (!currentNamespace.isNullOrEmpty()) {
                "KubeLog - $currentNamespace"
            } else {
                "Kubelog"
            }
        }
    }

    LaunchedEffect(currentNamespace) {
        logTabsState.closeAll()
    }

    val isLoadedResult by produceState<LoadableResult<Unit>>(LoadableResult.Loading) {
        try {
            namespaces = NamespaceService.readAllNamespaceSuspending()
            currentNamespace = NamespaceService.readCurrentNamespaceSuspending()
            value = LoadableResult.Value(Unit)
        } catch (e: Exception) {
            value = LoadableResult.Error(e)
        }
    }

    Window(
        onCloseRequest = exitApplication,
        state = rememberWindowState(size = DpSize(900.dp, 600.dp)),
        title = windowTitle,
        icon = painterResource("AppIcon.png"),
        onPreviewKeyEvent = {
            if (it.type != KeyEventType.KeyDown) {
                return@Window false
            }
            when {
                it.isMetaPressed && it.key == Key.T -> {
                    podsListVisible = !podsListVisible
                    true
                }
                it.isMetaPressed && it.key == Key.F -> {
                    logTabsState.active?.search?.toggleVisible()
                    true
                }
                it.isMetaPressed && it.isShiftPressed && it.key == Key.W -> {
                    logTabsState.closeAll()
                    true
                }
                it.isMetaPressed && it.key == Key.W -> {
                    logTabsState.active?.let { active -> logTabsState.close(active) }
                    true
                }
                it.isMetaPressed && it.isShiftPressed && it.key == Key.C -> {
                    logTabsState.active?.clear()
                    true
                }
                else -> false
            }
        }
    ) {
        MenuBar {
            NamespacesMenu(namespaces, currentNamespace) {
                currentNamespace = it
            }
            TailLogsMenu(tailLogs) {
                tailLogs = it
            }
        }
        ThemeProvider {
            Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) {
                UpdateDialog()
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                ) {
                    val namespace = currentNamespace
                    when (val isLoaded = isLoadedResult) {
                        is LoadableResult.Loading -> LoadingView()
                        is LoadableResult.Error -> ErrorView(isLoaded.error.message ?: "")
                        is LoadableResult.Value ->
                            if (namespace == null) LoadingView()
                            else MainContent(namespace, tailLogs, podsListVisible, logTabsState)
                    }
                }
            }
        }
    }
}
