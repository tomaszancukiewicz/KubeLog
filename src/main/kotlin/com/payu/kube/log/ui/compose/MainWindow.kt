package com.payu.kube.log.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import com.payu.kube.log.service.NamespaceService
import com.payu.kube.log.ui.compose.component.ErrorView
import com.payu.kube.log.ui.compose.component.LoadingView
import com.payu.kube.log.ui.compose.component.ThemeProvider
import com.payu.kube.log.ui.compose.tab.LogTabsState
import com.payu.kube.log.util.LoadableResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@Composable
@Preview
fun MainWindow(exitApplication: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
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

    Window(title = windowTitle, onCloseRequest = exitApplication, icon = painterResource("AppIcon.png"), onPreviewKeyEvent = {
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
    }) {
        MenuBar {
            NamespacesMenu(namespaces, currentNamespace) {
                currentNamespace = it
            }
        }
        ThemeProvider {
            UpdateDialog()
            Surface(
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxSize()
            ) {
                val namespace = currentNamespace
                when (val isLoaded = isLoadedResult) {
                    is LoadableResult.Loading -> LoadingView()
                    is LoadableResult.Error -> ErrorView(isLoaded.error.message ?: "")
                    is LoadableResult.Value ->
                        if (namespace == null) LoadingView()
                        else MainContent(namespace, podsListVisible, logTabsState)
                }
            }
        }
    }
}
