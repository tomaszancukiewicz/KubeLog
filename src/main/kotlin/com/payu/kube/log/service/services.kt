package com.payu.kube.log.service

import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.namespaces.NamespaceService
import com.payu.kube.log.service.namespaces.NamespaceStoreService
import com.payu.kube.log.service.pods.PodService
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.search.SearchQueryCompilerService

val searchQueryCompilerService = SearchQueryCompilerService()
val podService = PodService()
val podStoreService = PodStoreService(podService)
val namespaceService = NamespaceService()
val namespaceStoreService = NamespaceStoreService(namespaceService, podStoreService)
val stylingTextService = StylingTextService()