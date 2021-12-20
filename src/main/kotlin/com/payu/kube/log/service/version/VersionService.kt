package com.payu.kube.log.service.version

import com.payu.kube.log.util.RegexUtils.getOrNull
import org.springframework.stereotype.Service

@Service
class VersionService {
    private val versionRegex = "v([0-9]+\\.[0-9]+\\.[0-9]+)(?:-.*)?".toRegex()

    fun extractVersionTable(versionText: String): List<Int>? {
        return versionRegex.find(versionText)?.groups
            ?.getOrNull(1)
            ?.value
            ?.split('.')
            ?.map { it.toInt() }
    }

    fun needsUpdate(onlineVersion: List<Int>, localVersion: List<Int>): Boolean {
        var i = 0
        while (i < localVersion.size && i < onlineVersion.size) {
            if (onlineVersion[i] > localVersion[i])
                return true
            if (onlineVersion[i] < localVersion[i])
                return false
            i++
        }
        return i < onlineVersion.size
    }
}