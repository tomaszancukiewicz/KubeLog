package com.payu.kube.log.service.search.query

abstract class Query {
    override fun toString(): String {
        return "Query()"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    open fun check(text: String): Boolean {
        return check(text, false)
    }

    open fun phrasesToMark(text: String): List<IntRange> {
        return phrasesToMark(text, false)
    }

    open fun check(text: String, ignoreCase: Boolean): Boolean {
        return false
    }

    open fun phrasesToMark(text: String, ignoreCase: Boolean): List<IntRange> {
        return listOf()
    }

    open fun toQueryString(): String {
        return ""
    }

    companion object {
        @JvmStatic
        protected fun wrapInBracketsWhenNeeded(query: Query): String {
            if (query is TextQuery || query is RegexQuery) {
                return query.toQueryString()
            }
            return "(${query.toQueryString()})"
        }
    }
}