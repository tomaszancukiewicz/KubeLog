package com.payu.kube.log.service.version.github

import com.payu.kube.log.service.config.HttpClientConfiguration
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GithubClientService {
    suspend fun getLatestReleaseUrl(): Release = withContext(Dispatchers.IO) {
        HttpClientConfiguration.client
            .get("https://api.github.com/repos/tomaszancukiewicz/KubeLog/releases/latest")
            .body()
    }
}