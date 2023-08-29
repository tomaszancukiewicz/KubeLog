package com.payu.kube.log.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.payu.kube.log.service.config.BuildPropertiesConfiguration
import com.payu.kube.log.service.version.VersionService
import com.payu.kube.log.service.version.github.GithubClientService
import com.payu.kube.log.service.version.github.Release

data class UpdateData(
    val release: Release,
    val newestVersion: List<Int>,
    val localVersion: List<Int>
)

@Composable
fun UpdateDialog() {
    var isDialogVisible by remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    val updateData by produceState<UpdateData?>(null) {
        val latestRelease: Release = runCatching {
            GithubClientService.getLatestReleaseUrl()
        }.getOrNull() ?: return@produceState
        val newestVersion = VersionService.extractVersionTable(latestRelease.tagName) ?: return@produceState
        val localVersion = VersionService.extractVersionTable("v${BuildPropertiesConfiguration.version}") ?: return@produceState
        if (VersionService.needsUpdate(newestVersion, localVersion)) {
            value = UpdateData(latestRelease, newestVersion, localVersion)
        }
    }

    updateData
        ?.takeIf { isDialogVisible }
        ?.let {
            val localVersionString = it.localVersion.joinToString(".")
            val onlineVersionString = it.newestVersion.joinToString(".")

            AlertDialog(
                title = { Text("Information") },
                text = {
                    Column(modifier = Modifier.requiredWidth(IntrinsicSize.Max)) {
                        Text("There is newer version of the app.")
                        Text("Yours: $localVersionString Online: $onlineVersionString")
                    }
               },
                confirmButton = {
                    Button(onClick = {
                        uriHandler.openUri(it.release.htmlUrl)
                        isDialogVisible = false
                    }) {
                        Text("Open browser")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        isDialogVisible = false
                    }) {
                        Text("Close")
                    }
                },
                onDismissRequest = {},
            )
        }
}