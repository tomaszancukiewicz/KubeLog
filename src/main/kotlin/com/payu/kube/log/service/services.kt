package com.payu.kube.log.service

import com.payu.kube.log.service.namespaces.NamespaceService
import com.payu.kube.log.service.pods.PodService
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.service.version.VersionService

val searchQueryCompilerService = SearchQueryCompilerService()
val podService = PodService()
val podStoreService = PodStoreService(podService)
val namespaceService = NamespaceService()
val versionService = VersionService()