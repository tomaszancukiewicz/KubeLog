package com.payu.kube.log.service.version.github

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class GithubConfig {

    @Bean
    fun githubRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .rootUri("https://api.github.com")
            .setConnectTimeout(Duration.ofSeconds(3))
            .build()
    }
}