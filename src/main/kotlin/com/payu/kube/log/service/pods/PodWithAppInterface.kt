package com.payu.kube.log.service.pods

import com.payu.kube.log.model.PodInfo

fun interface PodWithAppInterface {
    fun onNewPodWithApp(pod: PodInfo)
}