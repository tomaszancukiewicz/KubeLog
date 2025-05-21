package com.kube.log.util

object RegexUtils {
    fun MatchGroupCollection.getOrNull(index: Int): MatchGroup? {
        return this.takeIf { index < it.size }?.get(index)
    }
}