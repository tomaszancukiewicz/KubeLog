package com.payu.kube.log.stage

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.namespaces.NamespaceStoreService
import javafx.beans.binding.Bindings
import javafx.scene.image.Image
import javafx.stage.Stage

@Component
class MainStageInitializer(
    private val applicationContext: ApplicationContext,
    @Value("classpath:/fxmls/main.fxml")
    private val mainFxmlResource: Resource,
    @Value("classpath:/css.css")
    private val cssResource: Resource,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val namespaceStoreService: NamespaceStoreService
) : ApplicationListener<StageReadyEvent> {

    override fun onApplicationEvent(event: StageReadyEvent) {
        val fxmlLoader = FXMLLoader(mainFxmlResource.url)
        fxmlLoader.setControllerFactory {
            applicationContext.getBean(it)
        }
        val parent = fxmlLoader.load<Parent>()
        val scene = Scene(parent, 1000.0, 600.0)
        scene.stylesheets.add(cssResource.url.toExternalForm())
        scene.setOnKeyPressed {
            globalKeyEventHandlerService.onKeyPressed(it)
        }
        event.stage.scene = scene
        setTitle(event.stage)
        event.stage.show()
    }

    private fun setTitle(stage: Stage) {
        val isNameSpaceSet = namespaceStoreService.currentNamespace.isNotNull
            .and(namespaceStoreService.currentNamespace.isNotEmpty)
        val titleBinding = Bindings.`when`(isNameSpaceSet)
            .then(Bindings.concat("KubeLog - ", namespaceStoreService.currentNamespace))
            .otherwise("KubeLog")
        stage.titleProperty().bind(titleBinding)
    }
}
