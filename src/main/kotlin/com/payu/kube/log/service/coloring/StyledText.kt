package com.payu.kube.log.service.coloring

data class StyledText(val text: String, val styledStyledSegments: List<StyledSegment>, val appliedStyles: Set<String>) {
    constructor(text: String): this(text, listOf(StyledSegment(listOf(), text.indices)), setOf())

    fun createSegments(): List<Pair<String, List<String>>> {
        return styledStyledSegments.map {
            text.substring(it.range) to it.styles
        }
    }
}