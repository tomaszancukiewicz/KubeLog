package com.payu.kube.log.service.pods

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodJsonWrapper
import com.payu.kube.log.model.PodListState
import java.util.concurrent.Executors
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

@Service
class PodService {
    val POD_WATCHING_TERMINATION_SEQUENCE = "\n}\n"

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

