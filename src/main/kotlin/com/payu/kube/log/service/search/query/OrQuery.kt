package com.payu.kube.log.service.search.query

class OrQuery(q1: Query, q2: Query) : BinaryOperationQuery(q1, q2) {
    override fun toString(): String {
        return "OrQuery($q1, $q2, errors=$errors)"
    }

    override fun check(text: String): Boolean {
        return q1.check(text) || q2.check(text)
    }
}