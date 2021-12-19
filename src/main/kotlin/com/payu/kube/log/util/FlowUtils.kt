package com.payu.kube.log.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

object FlowUtils {
    fun <T, R> Flow<T>.zipWithNext(transform: (T?, T) -> R): Flow<R> = flow {
        var prev: T? = null
        collect { value ->
            emit(transform(prev, value))
            prev = value
        }
    }
}