package com.payu.kube.log.ui.compose

import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBarScope

@Composable
fun MenuBarScope.NamespacesMenu(
    namespaces: List<String>, currentNamespace: String?, onChangeCurrentNamespace: (String) -> Unit
) {
    Menu("Namespaces", mnemonic = 'N') {
        for (namespace in namespaces) {
            CheckboxItem(
                namespace,
                checked = currentNamespace == namespace,
                onCheckedChange = { onChangeCurrentNamespace(namespace) }
            )
        }
    }
}