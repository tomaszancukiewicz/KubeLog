package com.payu.kube.log.model

import java.time.Instant

data class PodInfo(
    val uid: String,
    val name: String,
    val appNameLabel: String,
    val ownerReferencesName: String,
    val namespace: String,
    val containerImage: String,
    val containerName: String,
    val nodeName: String,
    val containerCount: Int,
    val startedCount: Int,
    val readyCount: Int,
    val restarts: Int,
    val state: PodState,
    val phase: String,
    val creationTimestamp: Instant,
    val deletionTimestamp: Instant?
) {

    val calculatedAppName: String by lazy {
        val appNameLabelValue = appNameLabel
            .takeIf { it.isNotBlank() }
            ?.takeIf { name.startsWith(it) }
        if (appNameLabelValue != null) {
            return@lazy appNameLabelValue
        }
        val ownerReferenceNameValue = ownerReferencesName
            .takeIf { it.isNotBlank() }
            ?.takeIf { name.startsWith(it) }
        if (ownerReferenceNameValue != null) {
            return@lazy ownerReferenceNameValue
        }
        val containterNameValue = containerName
            .takeIf { it.isNotBlank() }
            ?.takeIf { name.startsWith(it) }
        if (containterNameValue != null) {
            return@lazy containterNameValue
        }
        return@lazy name
    }

    val isReady: Boolean
        get() = readyCount == containerCount

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? PodInfo ?: return false
        return name == o.name
    }

    fun canBeRemoved(instant: Instant = Instant.now()): Boolean {
        return deletionTimestamp != null && deletionTimestamp.plusSeconds(5).isBefore(instant)
    }
}