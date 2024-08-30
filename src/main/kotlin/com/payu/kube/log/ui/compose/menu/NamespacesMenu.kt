package com.payu.kube.log.ui.compose.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.MenuBarScope
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MenuBarScope.NamespacesMenu(
    namespacesFlow: StateFlow<List<String>>,
    currentNamespaceFlow: StateFlow<String?>,
    onChangeCurrentNamespace: (String) -> Unit
) {
    val namespaces by namespacesFlow.collectAsState()
    val currentNamespace by currentNamespaceFlow.collectAsState()

    Menu("Namespaces", mnemonic = 'N') {
        for (namespace in namespaces) {
            RadioButtonItem(
                namespace,
                selected = currentNamespace == namespace,
                onClick = { onChangeCurrentNamespace(namespace) }
            )
        }
    }
}