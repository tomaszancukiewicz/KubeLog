package com.payu.kube.log.service.search.query

class NotQuery(q: Query) : UnaryOperationQuery(q) {
    override fun toString(): String {
        return "NotQuery($q, errors=$errors)"
    }

    override fun check(text: String): Boolean {
        return !super.check(text)
    }
}