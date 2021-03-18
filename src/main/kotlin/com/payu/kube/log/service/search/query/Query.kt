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
        return false
    }

    open fun phrasesToMark(text: String): List<IntRange> {
        return listOf()
    }
}