package com.payu.kube.log

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.payu.kube.log.stage.StageReadyEvent
import com.payu.kube.log.ui.compose.MainWindow
import com.payu.kube.log.ui.compose.component.NotificationCenterProvider
import javafx.application.HostServices
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.function.Supplier
import javafx.application.Application as FXApplication

@SpringBootApplication
@EnableScheduling
@EnableAsync
class Application : FXApplication() {
    lateinit var applicationContext: ConfigurableApplicationContext

    override fun init() {
        super.init()
        applicationContext = SpringApplicationBuilder(Application::class.java)
            .headless(false)
            .initializers(object : ApplicationContextInitializer<GenericApplicationContext> {
                override fun initialize(applicationContext: GenericApplicationContext) {
                    applicationContext.registerBean(FXApplication::class.java, Supplier { this@Application })
                    applicationContext.registerBean(Parameters::class.java, Supplier { parameters })
                    applicationContext.registerBean(HostServices::class.java, Supplier { hostServices })
                }
            })
            .run(*parameters.raw.toTypedArray())
    }

    override fun start(primaryStage: Stage) {
        applicationContext.publishEvent(StageReadyEvent(primaryStage))
    }

    override fun stop() {
        super.stop()
        applicationContext.close()
        Platform.exit()
    }
}

//fun main(args: Array<String>) {
//    FXApplication.launch(Application::class.java, *args)
//}

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
