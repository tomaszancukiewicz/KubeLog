package com.payu.kube.log.service.version

import com.payu.kube.log.service.version.github.GithubClientService
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.util.RegexUtils.getOrNull
import javafx.application.HostServices
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import org.springframework.boot.info.BuildProperties
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class UpdaterService(
    private val buildProperties: BuildProperties,
    private val hostServices: HostServices,
    private val githubClientService: GithubClientService
) {

    private val log = logger()
    private val versionRegex = "v([0-9]+\\.[0-9]+\\.[0-9]+)(?:-.*)?".toRegex()

    @Async
    fun checkVersion() {
        val latestRelease = runCatching { githubClientService.getLatestReleaseUrl() }
            .getOrNull() ?: return
        val newestVersion = extractVersionTable(latestRelease.tagName) ?: return
        log.info("Latest release: $latestRelease")
        val localVersion = extractVersionTable("v${buildProperties.version}") ?: return

        if (!needsUpdate(newestVersion, localVersion))
            return

        log.info("App needs update newestVersion=$newestVersion localVersion=$localVersion")

        Platform.runLater {
            val localVersionString = localVersion.joinToString(".")
            val onlineVersionString = newestVersion.joinToString(".")
            Alert(Alert.AlertType.INFORMATION,
                "There is newer version of the app.\nYours: $localVersionString Online: $onlineVersionString",
                ButtonType.CLOSE, ButtonType("Open browser", ButtonBar.ButtonData.APPLY)
            ).showAndWait().ifPresent {
                if (it.buttonData == ButtonBar.ButtonData.APPLY) {
                    hostServices.showDocument(latestRelease.htmlUrl)
                }
            }
        }
    }

    private fun extractVersionTable(versionText: String): List<Int>? {
        return versionRegex.find(versionText)?.groups
            ?.getOrNull(1)
            ?.value
            ?.split('.')
            ?.map { it.toInt() }
    }

    private fun needsUpdate(onlineVersion: List<Int>, localVersion: List<Int>): Boolean {
        var i = 0
        while (i < localVersion.size && i < onlineVersion.size) {
            if (onlineVersion[i] > localVersion[i])
                return true
            i++
        }
        return i < onlineVersion.size
    }
}