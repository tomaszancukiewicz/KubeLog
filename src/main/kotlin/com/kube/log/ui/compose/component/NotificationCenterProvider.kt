package com.kube.log.ui.compose.component

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.SystemTray
import java.awt.TrayIcon

val NotificationCenter = staticCompositionLocalOf { TrayState() }

val isTraySupported: Boolean get() = SystemTray.isSupported()

@Composable
fun NotificationCenterProvider(
    trayState: TrayState = rememberTrayState(),
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val icon = painterResource("AppIcon.png")
    val localDensity = LocalDensity.current
    val trayIcon = remember {
        val image = icon.toAwtImage(localDensity, LayoutDirection.Ltr, Size(16f, 16f))
        TrayIcon(image, "KubeLog").also { it.isImageAutoSize = true }
    }

    LaunchedEffect(Unit) {
        trayState.notificationFlow
            .filter { isTraySupported }
            .onEach {
                SystemTray.getSystemTray().add(trayIcon)
                trayIcon.displayMessage(it.title, it.message, TrayIcon.MessageType.INFO)
                SystemTray.getSystemTray().remove(trayIcon)
            }
            .launchIn(coroutineScope)
    }

    CompositionLocalProvider(
        NotificationCenter provides trayState,
        content = content
    )
}