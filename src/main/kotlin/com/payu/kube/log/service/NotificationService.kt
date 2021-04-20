package com.payu.kube.log.service

import com.payu.kube.log.util.LoggerUtils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.swing.SwingUtilities


@Service
class NotificationService(
    @Value("classpath:/AppIcon.png")
    private val iconResource: Resource
) {

    private val log = logger()

    private var systemTray: SystemTray? = null

    @PostConstruct
    private fun init() {
        SwingUtilities.invokeLater {
            if (SystemTray.isSupported()) {
                systemTray = SystemTray.getSystemTray()
            }
        }
    }

    fun showNotification(caption: String, text: String) {
        SwingUtilities.invokeLater {
            val systemTray = systemTray ?: return@invokeLater
            val image = Toolkit.getDefaultToolkit().getImage(iconResource.url)
            log.info("Show notification: $caption $text")

            val trayIcon = TrayIcon(image, "KubeLog")
            trayIcon.isImageAutoSize = true

            systemTray.add(trayIcon)
            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO)
            systemTray.remove(trayIcon)
        }
    }

    @PreDestroy
    fun destroy() {
        SwingUtilities.invokeLater {
            systemTray?.trayIcons?.forEach {
                systemTray?.remove(it)
            }
        }
    }
}