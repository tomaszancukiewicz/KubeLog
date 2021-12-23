package com.payu.kube.log.service.pods

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodJsonWrapper
import com.payu.kube.log.util.LoggerUtils.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Service
class PodService {
    companion object {
        private const val POD_WATCHING_TERMINATION_SEQUENCE = "\n}\n"
    }

    private val log = logger()
    private val objectMapper = ObjectMapper()

    @ExperimentalCoroutinesApi
    fun monitorPods(namespace: String): Flow<List<PodInfo>> = channelFlow {
        val podMap = readAllPodsSuspending(namespace)
            .associateBy { it.name }
            .toMutableMap()
        send(podMap.values.toList())

        val watchingJob = async {
            while (isActive) {
                watchingPodsSuspending(namespace)
                    .collect {
                        podMap[it.name] = it
                        send(podMap.values.toList())
                    }
            }
        }

        val removingJob = async {
            while (isActive) {
                val now = Instant.now()
                val podsToDelete = podMap.values.toList().filter { it.canBeRemoved(now) }
                if (podsToDelete.isNotEmpty()) {
                    podsToDelete.forEach {
                        podMap.remove(it.name)
                    }

                    send(podMap.values.toList())
                }
                delay(1000)
            }
        }

        removingJob.await()
        watchingJob.await()
    }

    private suspend fun readAllPodsSuspending(namespace: String): List<PodInfo> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            log.info("Start read all pods")
            val p = ProcessBuilder(
                "/usr/local/bin/kubectl", "--namespace", namespace,
                "get", "pods", "-o", "json"
            )
                .redirectErrorStream(true)
                .start()
            continuation.invokeOnCancellation {
                p.destroy()
            }
            val reader = p.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            val exitValue = p.onExit().get().exitValue()
            log.info("Stop read all pods $exitValue")
            if (exitValue != 0 || output.isEmpty()) {
                continuation.resumeWithException(IllegalStateException("No data\nexit code: $exitValue"))
                return@suspendCancellableCoroutine
            }

            val jsonNode = runCatching { objectMapper.readTree(output) }.getOrNull()
            if (jsonNode == null) {
                continuation.resumeWithException(IllegalStateException("Wrong output\n$output\nexit code: $exitValue"))
                return@suspendCancellableCoroutine
            }

            if (jsonNode.path("kind").asText() != "List") {
                continuation.resumeWithException(IllegalStateException("Wrong output\n$output\nexit code: $exitValue"))
                return@suspendCancellableCoroutine
            }

            continuation.resume(jsonNode.path("items").mapNotNull { parsePod(it) })
            log.info("Stop processing all pods")
        }
    }

    @ExperimentalCoroutinesApi
    private fun watchingPodsSuspending(namespace: String): Flow<PodInfo> = channelFlow {
        watchingPodsSuspending(namespace) {
            trySendBlocking(it).isSuccess
        }
    }

    private suspend fun watchingPodsSuspending(namespace: String, onNewPod: (PodInfo) -> Boolean) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                log.info("Start watch pods")
                val p = ProcessBuilder(
                    "/usr/local/bin/kubectl", "--namespace", namespace,
                    "get", "pods", "-o", "json", "--watch"
                )
                    .redirectErrorStream(true)
                    .start()
                continuation.invokeOnCancellation {
                    p.destroy()
                }

                val reader = p.inputStream.bufferedReader()

                var buffer = ""
                var line: String?
                while (isActive) {
                    line = reader.readLine()
                    if (line == null) {
                        break
                    }
                    if (line.isEmpty()) {
                        continue
                    }
                    buffer += "$line\n"

                    val startIndex = buffer.indexOf(POD_WATCHING_TERMINATION_SEQUENCE)
                    if (startIndex >= 0) {
                        val endIndex = startIndex + POD_WATCHING_TERMINATION_SEQUENCE.length
                        val wholeJson = buffer.substring(0, endIndex)
                        buffer = buffer.substring(endIndex)


                        val jsonNode = runCatching { objectMapper.readTree(wholeJson) }.getOrNull()
                        val isSent = parsePod(jsonNode)?.let(onNewPod) ?: true
                        if (!isSent) {
                            break;
                        }
                    }
                }
                reader.close()

                val exitValue = p.onExit().get().exitValue()
                log.info("Stop watch pods - $exitValue")

                if (isActive && exitValue != 0) {
                    continuation.resumeWithException(IllegalStateException("No data\nexit code: $exitValue"))
                } else {
                    continuation.resume(Unit)
                }
            }
        }

    private fun parsePod(item: JsonNode?): PodInfo? {
        return item
            ?.takeIf { it.path("kind").asText() == "Pod" }
            ?.let { PodJsonWrapper(it) }
            ?.create()
    }
}

