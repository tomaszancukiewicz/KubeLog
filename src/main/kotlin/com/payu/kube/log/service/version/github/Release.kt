package com.payu.kube.log.service.version.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val name: String,
    val body: String,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String,
)