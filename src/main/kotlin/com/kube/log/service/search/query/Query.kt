package com.kube.log.service.search.query

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

    abstract fun check(text: String): Boolean
    abstract fun phrasesToMark(text: String): List<IntRange>
}