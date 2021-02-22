package com.payu.kube.log.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import java.time.Instant

data class PodJsonWrapper(val json: JsonNode) {

    val objectMetadata: JsonNode
        get() = json.path("metadata")

    val uid: String
        get() = objectMetadata.path("uid").asText()

    val name: String
        get() = objectMetadata.path("name").asText()

    val appNameLabel: String
        get() = objectMetadata.path("labels").path("app.kubernetes.io/name").asText()

    val namespace: String
        get() = objectMetadata.path("namespace").asText()

    val creationTimestamp: String
        get() = objectMetadata.path("creationTimestamp").asText()

    val deletionTimestamp: String?
        get() = objectMetadata.path("deletionTimestamp").textValue()

    val podSpec: JsonNode
        get() = json.path("spec")

    val containerImage: String
        get() = podSpec.path("containers").firstOrNull()?.path("image")?.asText() ?: ""

    val containerName: String
        get() = podSpec.path("containers").firstOrNull()?.path("name")?.asText() ?: ""

    val nodeName: String
        get() = podSpec.path("nodeName").asText()

    val podStatus: JsonNode
        get() = json.path("status")

    val phase: String
        get() = podStatus.path("phase").asText()

    val containerStatuses: JsonNode
        get() = podStatus.path("containerStatuses")

    val readyCount: Int
        get() = containerStatuses.count { it.path("ready").asBoolean() }

    val startedCount: Int
        get() = containerStatuses.count { it.path("started").asBoolean() }

    val restarts: Int
        get() = containerStatuses.sumBy { it.path("restartCount").asInt() }

    val states: JsonNode
        get() = containerStatuses.firstOrNull()?.path("state") ?: NullNode.getInstance()

    val stateCode: String
        get() = states.fields().asSequence().firstOrNull()?.key ?: "waiting"

    val stateObj: JsonNode
        get() = states.path(stateCode)

    val state: PodState
        get() {
            val stateString = stateCode
            val stateObject = stateObj
            return when(stateString) {
                "running" -> PodState.Running
                "terminated" -> {
                    val reason = stateObject.path("reason").asText()
                    val exitCode = stateObject.path("exitCode").asInt()
                    val finishedAt = stateObject.path("finishedAt").asText()
                    PodState.Terminated(reason, exitCode, finishedAt)
                }
                "waiting" -> PodState.Waiting(stateObject.path("reason").asText())
                else -> PodState.Waiting()
            }
        }

    fun create(): PodInfo {
        val creationTimestampInstant = Instant.parse(creationTimestamp)
        val deletionTimestampInstant = deletionTimestamp?.let { Instant.parse(it) }
        return PodInfo(
            uid, name, appNameLabel, namespace, containerImage, containerName, nodeName,
            containerStatuses.count(),
            startedCount, readyCount, restarts,
            state, phase,
            creationTimestampInstant, deletionTimestampInstant
        )
    }
}