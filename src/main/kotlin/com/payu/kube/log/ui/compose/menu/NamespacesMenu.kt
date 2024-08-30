package com.payu.kube.log.ui.compose.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.MenuBarScope

@Composable
fun MenuBarScope.NamespacesMenu(
    namespacesState: NamespacesState
) {
    val namespaces by namespacesState.namespaces.collectAsState()
    val currentNamespace by namespacesState.currentNamespace.collectAsState()

    Menu("Namespaces", mnemonic = 'N') {
        for (namespace in namespaces) {
            RadioButtonItem(
                namespace,
                selected = currentNamespace == namespace,
                onClick = { namespacesState.changeNamespace(namespace) }
            )
        }
    }
}