package com.kube.log.util

sealed class LoadableResult<out T> {
    object Loading : LoadableResult<Nothing>()
    data class Value<out T>(val value: T) : LoadableResult<T>()
    data class Error(val error: Throwable) : LoadableResult<Nothing>()
}