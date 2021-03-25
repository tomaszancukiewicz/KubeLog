package com.payu.kube.log.service.search.query

abstract class UnaryOperationQuery(val q: Query) : Query() {
    override fun toString(): String {
        return "UnaryOperationQuery($q, errors=$errors)"
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + q.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other))
            return false
        if (other !is UnaryOperationQuery)
            return false
        return q == other.q
    }

    override fun check(text: String): Boolean {
        return q.check(text)
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return q.phrasesToMark(text)
    }

    override fun toQueryString(): String {
        return q.toQueryString()
    }
}