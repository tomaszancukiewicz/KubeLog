package com.kube.log.service.coloring.rules

import com.kube.log.service.search.query.Query

open class ColoringQueryRule(private val query: Query) : ColoringRule() {
    override fun findFragments(text: String): List<IntRange> {
        return query.phrasesToMark(text)
    }
}