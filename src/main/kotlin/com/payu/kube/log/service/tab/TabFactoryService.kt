package com.payu.kube.log.service.tab

import com.payu.kube.log.ui.MainController
import javafx.fxml.FXMLLoader
import javafx.scene.control.Tab
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import com.payu.kube.log.ui.tab.TabController
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.logs.PodLogStoreService
import com.payu.kube.log.service.pods.PodStoreService

@Service
class TabFactoryService(
    @Value("classpath:/fxmls/tab.fxml")
    private val chartResource: Resource,
    private val podStoreService: PodStoreService,
    private val podLogStoreService: PodLogStoreService,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val stylingTextService: StylingTextService
) {

    fun createTab(pod: PodInfo, mainController: MainController): Tab {
        val fxmlLoader = FXMLLoader(chartResource.url)
        fxmlLoader.setController(
            TabController(
            pod, podStoreService, podLogStoreService, globalKeyEventHandlerService, stylingTextService,
            mainController
        )
        )
        return fxmlLoader.load()
    }
}