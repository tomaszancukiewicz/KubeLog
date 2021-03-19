package com.payu.kube.log.service.version.github

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class GithubClientService(private val githubRestTemplate: RestTemplate) {

    fun getLatestReleaseUrl(): Release {
        return githubRestTemplate
            .getForObject("/repos/tomaszancukiewicz/KubeLog/releases/latest")
    }
}