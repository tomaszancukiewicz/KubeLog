package com.payu.kube.log

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.payu.kube.log.ui.compose.MainWindow
import com.payu.kube.log.ui.compose.component.NotificationCenterProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

class Application

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
fun main(args: Array<String>) = application {
    NotificationCenterProvider {
        MainWindow(::exitApplication)
    }
}
