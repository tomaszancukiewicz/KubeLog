package com.kube.log.service.version

import com.kube.log.util.RegexUtils.getOrNull

data class VersionHolder(val table: List<Int>): Comparable<VersionHolder> {
    override fun compareTo(other: VersionHolder): Int {
        var i = 0
        while (i < table.size && i < other.table.size) {
            val c = table[i].compareTo(other.table[i])
            if (c != 0) {
                return c
            }
            i++
        }
        return table.size.compareTo(other.table.size)
    }

    override fun toString(): String {
        return table.joinToString(".")
    }
}

object VersionService {
    private val versionRegex = "v?([0-9]+\\.[0-9]+\\.[0-9]+)(?:-.*)?".toRegex()

    fun extractVersion(versionText: String): VersionHolder? {
        val table = versionRegex.find(versionText)?.groups
            ?.getOrNull(1)
            ?.value
            ?.split('.')
            ?.map { it.toInt() } ?: return null
        return VersionHolder(table)
    }

    fun needsUpdate(onlineVersion: VersionHolder, localVersion: VersionHolder): Boolean {
        return onlineVersion > localVersion
    }
}