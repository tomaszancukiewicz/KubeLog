package com.payu.kube.log.service.search.query

class NotQuery(q: Query) : UnaryOperationQuery(q) {
    override fun toString(): String {
        return "NotQuery($q)"
    }

    override fun check(text: String, ignoreCase: Boolean): Boolean {
        return !super.check(text, ignoreCase)
    }

    override fun toQueryString(): String {
        return "NOT ${wrapInBracketsWhenNeeded(q)}"
    }
}