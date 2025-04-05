package com.payu.kube.log.service.search.query

class AndQuery(q1: Query, q2: Query) : BinaryOperationQuery(q1, q2) {
    override fun toString(): String {
        return "AndQuery($q1, $q2)"
    }

    override fun check(text: String, ignoreCase: Boolean): Boolean {
        return q1.check(text, ignoreCase) && q2.check(text, ignoreCase)
    }

    override fun toQueryString(): String {
        return "${wrapInBracketsWhenNeeded(q1)} AND ${wrapInBracketsWhenNeeded(q2)}"
    }
}