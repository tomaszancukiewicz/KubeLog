package com.kube.log.service.version.github

import com.kube.log.service.config.HttpClientConfiguration
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GithubClientService {
    suspend fun getLatestReleaseUrl(): Release = withContext(Dispatchers.IO) {
        HttpClientConfiguration.client
            .get("https://api.github.com/repos/tomekancu/KubeLog/releases/latest")
            .body()
    }
}