package com.kube.log

import androidx.compose.ui.window.application
import com.kube.log.ui.compose.MainWindow
import com.kube.log.ui.compose.component.NotificationCenterProvider
import com.kube.log.ui.compose.component.SnackbarStateProvider

fun main() = application {
    NotificationCenterProvider {
        SnackbarStateProvider {
            MainWindow(::exitApplication)
        }
    }
}
