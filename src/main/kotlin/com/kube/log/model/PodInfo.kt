package com.kube.log.model

import com.kube.log.util.JsonElementUtils.asBoolean
import com.kube.log.util.JsonElementUtils.asInt
import com.kube.log.util.JsonElementUtils.asText
import com.kube.log.util.JsonElementUtils.jsonArrayOrNull
import com.kube.log.util.JsonElementUtils.jsonObjectOrNull
import com.kube.log.util.JsonElementUtils.path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
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
        get() = readyCount == containerCount && containerCount > 0

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

fun PodInfo(json: JsonElement): PodInfo {
    val objectMetadata = json.path("metadata")
    val uid = objectMetadata?.path("uid")?.asText() ?: ""
    val name = objectMetadata?.path("name")?.asText() ?: ""
    val appNameLabel = objectMetadata?.path("labels")?.path("app.kubernetes.io/name")?.asText() ?: ""
    val ownerReferencesName = objectMetadata?.path("ownerReferences")?.jsonArrayOrNull?.firstOrNull()
        ?.path("name")?.asText() ?: ""
    val namespace = objectMetadata?.path("namespace")?.asText() ?: ""
    val creationTimestamp = objectMetadata?.path("creationTimestamp")?.asText() ?: ""
    val deletionTimestamp = objectMetadata?.path("deletionTimestamp")?.asText()
    val podSpec = json.path("spec")
    val firstContainer = podSpec?.path("containers")?.jsonArrayOrNull?.firstOrNull()
    val containerImage = firstContainer?.path("image")?.asText() ?: ""
    val containerName = firstContainer?.path("name")?.asText() ?: ""
    val nodeName = podSpec?.path("nodeName")?.asText() ?: ""
    val podStatus = json.path("status")
    val phase = podStatus?.path("phase")?.asText() ?: ""
    val containerStatuses = podStatus?.path("containerStatuses")?.jsonArrayOrNull ?: buildJsonArray {}
    val readyCount = containerStatuses.count { it.path("ready")?.asBoolean() ?: false }
    val startedCount = containerStatuses.count { it.path("started")?.asBoolean() ?: false }
    val restarts = containerStatuses.sumOf { it.path("restartCount")?.asInt() ?: 0 }
    val states = containerStatuses.firstOrNull()?.path("state") ?: buildJsonObject {}
    val stateCode = states.jsonObjectOrNull?.keys?.firstOrNull() ?: "waiting"
    val stateObj = states.path(stateCode)
    val state: PodState = when(stateCode) {
        "running" -> PodState.Running
        "terminated" -> {
            val reason = stateObj?.path("reason")?.asText() ?: ""
            val exitCode = stateObj?.path("exitCode")?.asInt() ?: 0
            val finishedAt = stateObj?.path("finishedAt")?.asText() ?: ""
            PodState.Terminated(reason, exitCode, finishedAt)
        }
        "waiting" -> PodState.Waiting(stateObj?.path("reason")?.asText() ?: "")
        else -> PodState.Waiting()
    }
    val creationTimestampInstant = Instant.parse(creationTimestamp)
    val deletionTimestampInstant = deletionTimestamp?.let { Instant.parse(it) }
    return PodInfo(
        uid, name, appNameLabel, ownerReferencesName, namespace, containerImage, containerName, nodeName,
        containerStatuses.count(),
        startedCount, readyCount, restarts,
        state, phase,
        creationTimestampInstant, deletionTimestampInstant
    )
}