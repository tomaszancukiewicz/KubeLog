package com.payu.kube.log.service.pods

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodJsonWrapper
import com.payu.kube.log.model.PodListState
import com.payu.kube.log.util.LoggerUtils.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.Executors
import javax.annotation.PreDestroy
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Service
class PodService {
    companion object {
        private const val POD_WATCHING_TERMINATION_SEQUENCE = "\n}\n"
    }

    private val log = logger()
    private var listener: PodListChangeInterface? = null

    private var thread: Thread? = null
    private val objectMapper = ObjectMapper()

    @Volatile
    private var isReading = true
    private var process: Process? = null

    private var selectNamespaceExecutor = Executors.newSingleThreadExecutor {
        Executors.defaultThreadFactory()
            .newThread(it).apply {
                isDaemon = true
            }
    }

    fun startMonitorNamespace(namespace: String) {
        selectNamespaceExecutor.execute {
            listener?.onStateChange(PodListState.LoadingPods)
            listener?.onWholeList(mapOf())
            stopWatching()
            thread = thread(isDaemon = true, name = "watching-pods-thread") {
                isReading = true
                process = null
                readAllPods(namespace)
                isReading = false
                thread = null
            }
        }
    }

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

    private fun readAllPods(namespace: String) {
        log.info("Start read all pods")
        val p = ProcessBuilder(
            "/usr/local/bin/kubectl", "--namespace", namespace,
            "get", "pods", "-o", "json"
        )
            .redirectErrorStream(true)
            .start()
        process = p
        val reader = p.inputStream.bufferedReader()
        val output = reader.readText()
        reader.close()
        val exitValue = p.onExit().get().exitValue()
        log.info("Stop read all pods $exitValue")
        if (output.isEmpty()) {
            notifyStateChangeWhenRunningThread(PodListState.ErrorPods("No data\nexit code: $exitValue"))
            return
        }
        val jsonNode = runCatching { objectMapper.readTree(output) }.getOrNull()

        if (jsonNode == null) {
            notifyStateChangeWhenRunningThread(PodListState.ErrorPods("Wrong output\n$output\nexit code: $exitValue"))
            return
        }

        if (jsonNode.path("kind").asText() != "List") {
            notifyStateChangeWhenRunningThread(PodListState.ErrorPods("Wrong output\n$output\nexit code: $exitValue"))
            return
        }

        val podDict = mutableMapOf<String, PodInfo>()
        for (item in jsonNode.path("items")) {
            parsePod(item)?.let {
                podDict[it.name] = it
            }
        }
        listener?.onWholeList(podDict)
        notifyStateChangeWhenRunningThread(PodListState.Data)
        log.info("Stop processing all pods")
        watchingPods(namespace)
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

    private fun watchingPods(namespace: String) {
        var buffer = ""
        var exitValue = 0
        while (exitValue == 0 && isReading) {
            log.info("Start watch pods")
            val p = ProcessBuilder(
                "/usr/local/bin/kubectl", "--namespace", namespace,
                "get", "pods", "-o", "json", "--watch"
            )
                .redirectErrorStream(true)
                .start()
            process = p
            val reader = p.inputStream.bufferedReader()

            var line: String?
            do {
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
                    parsePod(jsonNode)?.let {
                        listener?.onPodChange(it)
                    }
                }
            } while (isReading)
            reader.close()

            exitValue = p.onExit().get().exitValue()
            log.info("Stop watch pods - $exitValue")
        }

        notifyStateChangeWhenRunningThread(PodListState.ErrorPods("Watcher exited\n$buffer\nexit code: $exitValue"))

        log.info("Perm stop watch pods - $exitValue")
    }

    private fun notifyStateChangeWhenRunningThread(podListState: PodListState) {
        if (!isReading) {
            return
        }
        listener?.onStateChange(podListState)
    }

    fun stopWatching() {
        isReading = false
        process?.destroy()
        thread?.join()
    }

    @PreDestroy
    fun destroy() {
        stopWatching()
        selectNamespaceExecutor.shutdown()
        log.info("Pod service destroy")
    }

    private fun parsePod(item: JsonNode?): PodInfo? {
        return item
            ?.takeIf { it.path("kind").asText() == "Pod" }
            ?.let { PodJsonWrapper(it) }
            ?.create()
    }

    fun registerOnNewPods(listener: PodListChangeInterface) {
        this.listener = listener
    }
}

