package com.payu.kube.log.service.pods

import com.payu.kube.log.model.PodInfo

fun interface PodChangeInterface {
    fun onPodChange(pod: PodInfo)
}