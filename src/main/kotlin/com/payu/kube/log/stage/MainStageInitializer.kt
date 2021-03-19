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
import com.payu.kube.log.service.IsDarkThemeService
import com.payu.kube.log.service.namespaces.NamespaceStoreService
import com.payu.kube.log.service.version.UpdaterService
import com.payu.kube.log.util.ViewUtils.toggleClass
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.stage.Stage

@Component
class MainStageInitializer(
    private val applicationContext: ApplicationContext,
    @Value("classpath:/fxmls/main.fxml")
    private val mainFxmlResource: Resource,
    @Value("classpath:/css.css")
    private val cssResource: Resource,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val namespaceStoreService: NamespaceStoreService,
    private val isDarkThemeService: IsDarkThemeService,
    private val updaterService: UpdaterService
) : ApplicationListener<StageReadyEvent> {

    companion object {
        private const val DARK_MODE_CLASS = "dark"
    }

    override fun onApplicationEvent(event: StageReadyEvent) {
        setupWindow(event.stage)
        updaterService.checkVersion()
    }

    private fun setupWindow(stage: Stage) {
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
        stage.scene = scene
        setStyle(parent)
        setTitle(stage)
        stage.show()
    }

    private fun setStyle(parent: Parent) {
        val changeStyleCallback: (observable: Observable) -> Unit = { _ ->
            parent.toggleClass(DARK_MODE_CLASS, isDarkThemeService.isDarkThemeProperty.value)
        }
        isDarkThemeService.isDarkThemeProperty.addListener(changeStyleCallback)
        changeStyleCallback(isDarkThemeService.isDarkThemeProperty)
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
