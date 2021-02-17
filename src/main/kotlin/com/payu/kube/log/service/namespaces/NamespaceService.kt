package com.payu.kube.log.service.namespaces

import org.springframework.stereotype.Service
import com.payu.kube.log.util.LoggerUtils.logger
import java.lang.IllegalStateException
import javax.annotation.PreDestroy

@Service
class NamespaceService {
    private val log = logger()

    private var process: Process? = null

    fun readAllNamespace(): List<String> {
        log.info("Start read all namespaces")
        val p = ProcessBuilder("/usr/local/bin/kubectl", "get", "namespaces",
            "-o=jsonpath={range .items[*].metadata.name}{@}{\"\\n\"}{end}")
            .redirectErrorStream(true)
            .start()
        process = p
        val reader = p.inputStream.bufferedReader()
        val output = reader.readText()
        reader.close()
        val exitValue = p.onExit().get().exitValue()
        log.info("Stop read all namespaces $exitValue")
        if (exitValue == 0) {
            return output.lines().filter { it.isNotEmpty() }
        }
        throw IllegalStateException("Wrong output\n$output\nexit code: $exitValue")
    }

    fun readCurrentNamespace(): String {
        log.info("Start read current namespace")
        val p = ProcessBuilder("/usr/local/bin/kubectl", "config", "view", "--minify",
            "-o=jsonpath={range .contexts[*].context.namespace}{@}{\"\\n\"}{end}")
            .redirectErrorStream(true)
            .start()
        process = p
        val reader = p.inputStream.bufferedReader()
        val output = reader.readText().trim()
        reader.close()
        val exitValue = p.onExit().get().exitValue()
        log.info("Stop read current namespace $exitValue")
        if (exitValue == 0 && output.isNotEmpty()) {
            return output
        }
        throw IllegalStateException("Wrong output\n$output\nexit code: $exitValue")
    }

    @PreDestroy
    fun destroy() {
        process?.destroy()
        log.info("Namespace service destroy")
    }
}

