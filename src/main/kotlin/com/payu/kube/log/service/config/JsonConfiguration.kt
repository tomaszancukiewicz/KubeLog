package com.payu.kube.log.service.config

import kotlinx.serialization.json.Json

object JsonConfiguration {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}