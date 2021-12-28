package com.payu.kube.log.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object FlowUtils {
    fun <T, R> Flow<T>.zipWithNext(transform: (T?, T) -> R): Flow<R> = flow {
        var prev: T? = null
        collect { value ->
            emit(transform(prev, value))
            prev = value
        }
    }

    @ExperimentalCoroutinesApi
    fun <T> Flow<T>.debounceWithCache(timeout: Long): Flow<List<T>> = channelFlow {
        val list = mutableListOf<T>()
        val listMutex = Mutex()

        val collectJob = async {
            collect { value ->
                listMutex.withLock {
                    list.add(value)
                }
            }
        }

        val emitJob = async {
            while (isActive) {
                val listToEmit = listMutex.withLock {
                    val l = list.toList()
                    list.clear()
                    l
                }

                send(listToEmit)
                delay(timeout)
            }
        }

        collectJob.await()
        emitJob.await()
    }
}