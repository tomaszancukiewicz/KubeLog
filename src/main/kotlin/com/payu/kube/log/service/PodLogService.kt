package com.payu.kube.log.service

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.util.LoggerUtils.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import java.time.temporal.ChronoUnit

object PodLogService {
    private val log = logger()

    fun watchingLogsSuspending(podFlow: StateFlow<PodInfo>): Flow<String> = channelFlow {
        send("waiting for logs...")
        waitForReady(podFlow)
        readLogsSuspending(podFlow.value) {
            trySendBlocking(it).isSuccess
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun waitForReady(podFlow: StateFlow<PodInfo>) {
        while (
            podFlow.value.creationTimestamp.plus(15, ChronoUnit.MINUTES).isAfter(Instant.now())
            && podFlow.value.state is PodState.Waiting
        ) {
            delay(100)
        }
    }

    private suspend fun readLogsSuspending(pod: PodInfo, onNewLine: (String) -> Boolean) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                log.info("start pod monitoring ${pod.name}")
                val p = ProcessBuilder(
                    "/usr/local/bin/kubectl", "--namespace", pod.namespace,
                    "logs", "-f", pod.name
                )
                    .redirectErrorStream(true)
                    .start()
                continuation.invokeOnCancellation {
                    p.destroy()
                }

                val reader = p.inputStream.bufferedReader()

                reader.use {
                    var line: String?
                    do {
                        line = it.readLine()
                        if (line == null) {
                            break
                        }
                        if (line.isEmpty()) {
                            continue
                        }

                        val isSent = onNewLine(line)
                        if (!isSent) {
                            break
                        }
                    } while (isActive)
                }

                val exitValue = p.waitFor()
                onNewLine("terminated with code: $exitValue")

                log.info("stop pod monitoring ${pod.name} - $exitValue")
            }
        }
}