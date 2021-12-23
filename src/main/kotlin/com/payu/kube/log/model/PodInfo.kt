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
        val containerNameValue = containerName
            .takeIf { it.isNotBlank() }
            ?.takeIf { name.startsWith(it) }
        if (containerNameValue != null) {
            return@lazy containerNameValue
        }
        return@lazy name
    }

    val isReady: Boolean
        get() = readyCount == containerCount

    fun isSamePod(podInfo: PodInfo): Boolean {
        return name == podInfo.name
    }

    fun canBeRemoved(instant: Instant = Instant.now()): Boolean {
        return deletionTimestamp != null && deletionTimestamp.plusSeconds(5).isBefore(instant)
    }

    companion object {
        val COMPARATOR =
            compareBy<PodInfo> { it.calculatedAppName }
                .thenBy { it.creationTimestamp }
                .thenBy { it.name }
    }
}