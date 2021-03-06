package com.payu.kube.log.service.search.query

abstract class Query {
    val errors = mutableListOf<String>()

    override fun toString(): String {
        return "Query(errors=$errors)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Query) {
            return false
        }
        return errors == other.errors
    }

    override fun hashCode(): Int {
        return errors.hashCode()
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