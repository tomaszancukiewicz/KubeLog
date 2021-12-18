package com.payu.kube.log.util

object ResultUtils {
    private val LOADING_ERROR = IllegalStateException("It is loading")

    fun <T> Result.Companion.loading(): Result<T> {
        return failure(LOADING_ERROR)
    }

    fun <T> Result<T>.toLoadableResult(): LoadableResult<T> {
        return fold({ LoadableResult.Value(it) }) {
            if (it === LOADING_ERROR) {
                LoadableResult.Loading
            } else {
                LoadableResult.Error(it)
            }
        }
    }

    val Result<*>.isLoading: Boolean get() = isFailure && exceptionOrNull() === LOADING_ERROR
}