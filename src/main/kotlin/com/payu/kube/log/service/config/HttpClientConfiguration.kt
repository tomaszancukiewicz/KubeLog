package com.payu.kube.log.service.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*

object HttpClientConfiguration {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(JsonConfiguration.json)
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 3_000
            requestTimeoutMillis = 3_000
            connectTimeoutMillis = 3_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
}