package com.payu.kube.log.service.version.github

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class Release(
    val name: String,
    val body: String,
    @SerialName("tag_name")
    @JsonProperty("tag_name")
    val tagName: String,
    @SerialName("html_url")
    @JsonProperty("html_url")
    val htmlUrl: String,
)