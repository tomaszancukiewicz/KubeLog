package com.payu.kube.log.service.version

import com.payu.kube.log.service.version.github.GithubClientService
import com.payu.kube.log.util.LoggerUtils.logger
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
    private val githubClientService: GithubClientService,
    private val versionService: VersionService
) {

    private val log = logger()

    @Async
    fun checkVersion() {
        val latestRelease = runCatching { githubClientService.getLatestReleaseUrl() }
            .getOrNull() ?: return
        val newestVersion = versionService.extractVersionTable(latestRelease.tagName) ?: return
        log.info("Latest release: $latestRelease")
        val localVersion = versionService.extractVersionTable("v${buildProperties.version}") ?: return

        if (!versionService.needsUpdate(newestVersion, localVersion))
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
}