package com.kube.log.service.config

import androidx.compose.ui.res.useResource
import java.util.*

object BuildPropertiesConfiguration {
    private val properties = useResource("META-INF/build-info.properties") {
        Properties().apply { load(it) }
    }
    val version: String
        get() = properties.getProperty("build.version", "0.0.0")
}