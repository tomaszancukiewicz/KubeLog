package com.payu.kube.log.service.search.query

abstract class UnaryOperationQuery(val q: Query) : Query() {
    override fun toString(): String {
        return "UnaryOperationQuery($q)"
    }

    override fun check(text: String): Boolean {
        return q.check(text)
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return q.phrasesToMark(text)
    }
}