package com.payu.kube.log.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import com.payu.kube.log.service.namespaceStoreService
import com.payu.kube.log.service.podStoreService
import com.payu.kube.log.ui.compose.tab.LogTabsState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
@Preview
fun MainWindow(exitApplication: () -> Unit) {
    val namespaces by namespaceStoreService.stateAllNamespacesSorted.collectAsState(listOf())
    val currentNamespace by namespaceStoreService.stateCurrentNamespace.collectAsState(null)
    val windowTitle by remember {
        derivedStateOf {
            if (!currentNamespace.isNullOrEmpty()) {
                "KubeLog - $currentNamespace"
            } else {
                "Kubelog"
            }
        }
    }
    var podsListVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val logTabsState = remember { LogTabsState(coroutineScope) }

    DisposableEffect(Unit) {
        podStoreService.init()
        namespaceStoreService.init()

        onDispose {
            podStoreService.destroy()
            namespaceStoreService.destroy()
        }
    }

    Window(title = windowTitle, onCloseRequest = exitApplication, onPreviewKeyEvent = {
        if (it.type != KeyEventType.KeyDown) {
            return@Window false
        }

        println("pressed ${it.isCtrlPressed} ${it.isMetaPressed} ${it.key}")
        when {
            it.isMetaPressed && it.key == Key.T -> {
                podsListVisible = !podsListVisible
                true
            }
            it.isMetaPressed && it.key == Key.C -> {
                logTabsState.active?.clear()
                true
            }
            it.isMetaPressed && it.key == Key.F -> {
                logTabsState.active?.search?.toggleVisible()
                true
            }
            it.isMetaPressed && it.key == Key.W -> {
                logTabsState.active?.let { active -> logTabsState.close(active) }
                true
            }
            else -> false
        }
    }) {
        MenuBar {
            NamespacesMenu(namespaces, currentNamespace) { namespaceStoreService.stateCurrentNamespace.value = it }
        }
        MaterialTheme {
            MainContent(currentNamespace, podsListVisible, logTabsState)
        }
    }
}
