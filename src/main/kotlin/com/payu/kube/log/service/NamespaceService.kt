package com.payu.kube.log.service

import com.payu.kube.log.util.LoggerUtils.logger
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object NamespaceService {
    private val log = logger()

    suspend fun readAllNamespaceSuspending(): List<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            log.info("Start read all namespaces")
            val p = ProcessBuilder("/usr/local/bin/kubectl", "get", "namespaces",
                "-o=jsonpath={range .items[*].metadata.name}{@}{\"\\n\"}{end}")
                .redirectErrorStream(true)
                .start()
            continuation.invokeOnCancellation {
                p.destroy()
            }
            val reader = p.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            val exitValue = p.onExit().get().exitValue()
            log.info("Stop read all namespaces $exitValue")
            if (exitValue == 0 && output.isNotEmpty()) {
                continuation.resume(output.lines().filter { it.isNotEmpty() })
            } else {
                continuation.resumeWithException(IllegalStateException("Wrong output\n$output\nexit code: $exitValue"))
            }
        }
    }

    suspend fun readCurrentNamespaceSuspending(): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            log.info("Start read current namespace")
            val p = ProcessBuilder("/usr/local/bin/kubectl", "config", "view", "--minify",
                "-o=jsonpath={range .contexts[*].context.namespace}{@}{\"\\n\"}{end}")
                .redirectErrorStream(true)
                .start()
            continuation.invokeOnCancellation {
                p.destroy()
            }
            val reader = p.inputStream.bufferedReader()
            val output = reader.readText().trim()
            reader.close()
            val exitValue = p.onExit().get().exitValue()
            log.info("Stop read current namespace $exitValue")
            if (exitValue == 0 && output.isNotEmpty()) {
                continuation.resume(output)
            } else {
                continuation.resumeWithException(IllegalStateException("Wrong output\n$output\nexit code: $exitValue"))
            }
        }
    }
}

