package com.payu.kube.log.service

import com.jthemedetecor.OsThemeDetector
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import org.springframework.stereotype.Service
import java.util.function.Consumer
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class IsDarkThemeService : Consumer<Boolean> {

    private val log = logger()
    private val detector = OsThemeDetector.getDetector()

    val isDarkThemeProperty = SimpleBooleanProperty(false)

    @PostConstruct
    fun init() {
        accept(detector.isDark)
        detector.registerListener(this)
    }

    override fun accept(isDark: Boolean) {
        Platform.runLater {
            isDarkThemeProperty.set(isDark)
        }
        log.info("Is dark-mode on $isDark")
    }

    @PreDestroy
    fun destroy() {
        detector.removeListener(this)
    }
}