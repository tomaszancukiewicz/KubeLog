package com.payu.kube.log.util

import com.payu.kube.log.util.ResultUtils.loading

sealed class LoadableResult<out T> {
    object Loading : LoadableResult<Nothing>()
    data class Value<out T>(val value: T) : LoadableResult<T>()
    data class Error(val error: Throwable) : LoadableResult<Nothing>()

    fun toResult(): Result<T> {
        return when (this) {
            is Loading -> Result.loading()
            is Error -> Result.failure(error)
            is Value -> Result.success(value)
        }
    }
}