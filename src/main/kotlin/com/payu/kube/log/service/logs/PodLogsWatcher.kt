package com.payu.kube.log.service.logs

import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import kotlin.concurrent.thread

data class PodLogsWatcher(var podFlow: StateFlow<PodInfo>) {
    private val log = logger()

    private var loaded = false
    private var isReading = true
    private var thread: Thread? = null
    private var process: Process? = null

    private val logs = mutableListOf<String>()

    private val pod: PodInfo
        get() = podFlow.value

    fun start() {
        if (loaded) {
            return
        }
        loaded = true
        thread = thread(isDaemon = true, name = "watching-logs-${pod.name}-thread") {
            startProcess()
        }
    }

    private fun startProcess() {
        log.info("waiting for creation")
        synchronized(logs) {
            logs.add("waiting for logs")
        }
        while(isReading &&
            pod.creationTimestamp.plusSeconds(15).isAfter(Instant.now()) &&
            pod.state != PodState.Running) {
            Thread.sleep(100)
        }
        if (!isReading) {
            return
        }
        log.info("start pod monitoring ${pod.name}")
        val p = ProcessBuilder("/usr/local/bin/kubectl", "--namespace", pod.namespace,
            "logs", "-f", pod.name)
            .redirectErrorStream(true)
            .start()
        process = p
        val reader = p.inputStream.bufferedReader()

        var line : String?
        do {
            line = reader.readLine()
            if (line == null) {
                break
            }
            if (line.isEmpty()){
                continue
            }
            synchronized(logs) {
                logs.add(line)
            }
        } while (isReading)
        reader.close()

        val exitValue = p.onExit().get().exitValue()
        isReading = false
        synchronized(logs) {
            logs.add("terminated with code: $exitValue")
        }
        log.info("stop pod monitoring ${pod.name} - $exitValue")
    }

    fun getNewLogs(): List<String> {
        return synchronized(logs) {
            val r = logs.toList()
            logs.clear()
            r
        }
    }

    fun stop() {
        isReading = false
        process?.destroy()
        thread?.join()
    }
}