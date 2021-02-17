package com.payu.kube.log

import com.payu.kube.log.stage.StageReadyEvent
import javafx.application.HostServices
import javafx.application.Platform
import javafx.stage.Stage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.function.Supplier
import javafx.application.Application as FXApplication

@SpringBootApplication
class Application : FXApplication() {
    lateinit var applicationContext: ConfigurableApplicationContext

    override fun init() {
        super.init()
        applicationContext = SpringApplicationBuilder(Application::class.java)
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

fun main(args: Array<String>) {
    FXApplication.launch(Application::class.java, *args)
}
