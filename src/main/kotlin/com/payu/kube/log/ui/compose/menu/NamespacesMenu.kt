package com.payu.kube.log.ui.compose.menu

import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBarScope

@Composable
fun MenuBarScope.NamespacesMenu(
    namespaces: List<String>, currentNamespace: String?, onChangeCurrentNamespace: (String) -> Unit
) {
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