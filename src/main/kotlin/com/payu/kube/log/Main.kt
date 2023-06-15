package com.payu.kube.log

import androidx.compose.ui.window.application
import com.payu.kube.log.ui.compose.MainWindow
import com.payu.kube.log.ui.compose.component.NotificationCenterProvider
import com.payu.kube.log.ui.compose.component.SnackbarStateProvider

fun main() = application {
    NotificationCenterProvider {
        SnackbarStateProvider {
            MainWindow(::exitApplication)
        }
    }
}
