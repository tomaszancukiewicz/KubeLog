package com.payu.kube.log.service.pods

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodListState

interface PodChangeInterface {
    fun onPodChange(pod: PodInfo)
}