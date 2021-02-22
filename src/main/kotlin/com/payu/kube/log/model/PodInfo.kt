package com.payu.kube.log.model

import java.time.Instant

data class PodInfo(
    val uid: String,
    val name: String,
    val appNameLabel: String,
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