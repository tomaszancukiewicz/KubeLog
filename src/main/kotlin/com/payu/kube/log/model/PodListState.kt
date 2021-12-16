package com.payu.kube.log.model

sealed class PodListState {
    object LoadingPods : PodListState()
    object Data : PodListState()
    data class ErrorPods(val message: String? = null) : PodListState()
}
