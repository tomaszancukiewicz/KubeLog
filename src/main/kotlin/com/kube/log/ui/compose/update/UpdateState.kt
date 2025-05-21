package com.kube.log.ui.compose.update

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kube.log.service.config.BuildPropertiesConfiguration
import com.kube.log.service.version.VersionHolder
import com.kube.log.service.version.VersionService
import com.kube.log.service.version.github.GithubClientService
import com.kube.log.service.version.github.Release

data class UpdateData(
    val release: Release,
    val newestVersion: VersionHolder,
    val localVersion: VersionHolder
)

class UpdateState {
    private var isDialogVisible by mutableStateOf(true)
    private var updateData by mutableStateOf<UpdateData?>(null)
    val visibleUpdate by derivedStateOf {
        updateData?.takeIf { isDialogVisible }
    }

    suspend fun loadData() {
        val latestRelease: Release = runCatching { GithubClientService.getLatestReleaseUrl() }.getOrNull() ?: return
        val newestVersion = VersionService.extractVersion(latestRelease.tagName) ?: return
        val localVersion = VersionService.extractVersion(BuildPropertiesConfiguration.version) ?: return
        if (VersionService.needsUpdate(newestVersion, localVersion)) {
            updateData = UpdateData(latestRelease, newestVersion, localVersion)
        }
    }

    fun closeDialog() {
        isDialogVisible = false
    }
}