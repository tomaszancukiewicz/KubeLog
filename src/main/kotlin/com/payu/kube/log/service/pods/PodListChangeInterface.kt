package com.payu.kube.log.service.pods

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodListState

interface PodListChangeInterface : PodChangeInterface {
    fun onWholeList(map: Map<String, PodInfo>)
    fun onStateChange(state: PodListState)
}