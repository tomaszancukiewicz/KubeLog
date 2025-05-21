package com.kube.log.ui.compose.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun UpdateDialog() {
    val uriHandler = LocalUriHandler.current
    val updateState = remember { UpdateState() }

    LaunchedEffect(Unit) {
        updateState.loadData()
    }

    updateState.visibleUpdate
        ?.let {
            AlertDialog(
                title = { Text("Information") },
                text = {
                    Column(modifier = Modifier.requiredWidth(IntrinsicSize.Max)) {
                        Text("There is newer version of the app.")
                        Text("Yours: ${it.localVersion} Online: ${it.newestVersion}")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        uriHandler.openUri(it.release.htmlUrl)
                        updateState.closeDialog()
                    }) {
                        Text("Open browser")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        updateState.closeDialog()
                    }) {
                        Text("Close")
                    }
                },
                onDismissRequest = {},
            )
        }
}