package com.payu.kube.log.service.search.query

abstract class Query {
    override fun toString(): String {
        return "Query()"
    }

    open fun check(text: String): Boolean {
        return false
    }

    open fun phrasesToMark(text: String): List<IntRange> {
        return listOf()
    }
}