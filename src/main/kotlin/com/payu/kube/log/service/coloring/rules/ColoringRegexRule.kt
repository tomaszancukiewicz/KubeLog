package com.payu.kube.log.service.coloring.rules

open class ColoringRegexRule(
    coloringClass: List<String>, private val regex: Regex
) : ColoringRule(coloringClass) {
    override fun findFragments(text: String): List<IntRange> {
        return regex.findAll(text)
            .map(this::extractRange)
            .filterNotNull()
            .toList()
    }

    protected open fun extractRange(matchResult: MatchResult): IntRange? {
        var matchGroup: MatchGroup? = null
        for (i in 1 until matchResult.groups.size) {
            val group = matchResult.groups[i]
            if (group != null) {
                matchGroup = group
                break
            }
        }
        return matchGroup?.range ?: matchResult.range
    }
}