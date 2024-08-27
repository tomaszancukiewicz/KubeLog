package com.payu.kube.log.ui.compose.menu

import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBarScope

@Composable
fun MenuBarScope.TailLogsMenu(
    tail: Boolean, onChangeTail: (Boolean) -> Unit
) {
    Menu("Tail logs", mnemonic = 'T') {
        CheckboxItem(
            "Enabled",
            checked = tail,
            onCheckedChange = onChangeTail
        )
    }
}