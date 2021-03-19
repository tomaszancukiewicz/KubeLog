package com.payu.kube.log.service.version.github

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Release(
    val name: String,
    val body: String,
    @JsonProperty("tag_name")
    val tagName: String,
    @JsonProperty("html_url")
    val htmlUrl: String,
)