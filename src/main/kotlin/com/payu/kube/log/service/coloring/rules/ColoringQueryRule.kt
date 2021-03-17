package com.payu.kube.log.service.coloring.rules

import com.payu.kube.log.service.search.query.Query

open class ColoringQueryRule(
    coloringClass: List<String>, private val query: Query
) : ColoringRule(coloringClass) {
    override fun findFragments(text: String): List<IntRange> {
        return query.phrasesToMark(text)
    }
}