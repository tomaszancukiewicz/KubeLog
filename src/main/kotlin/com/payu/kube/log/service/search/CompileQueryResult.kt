package com.payu.kube.log.service.search

import com.payu.kube.log.service.search.query.Query

data class CompileQueryResult(
    val query: Query,
    val errors: List<String>
)