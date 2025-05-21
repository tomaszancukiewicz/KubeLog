package com.kube.log.service.coloring

import com.kube.log.service.coloring.rules.ColoringRegexRule
import com.kube.log.util.RegexUtils.getOrNull

object Rules {
    val HTTP_METHODS_RULE = ColoringRegexRule(
        "(?<=[^a-z-A-Z])(GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)(?=[^a-z-A-Z])".toRegex()
    )
    val ERROR_LOG_LEVEL_RULE = object : ColoringRegexRule(
        "^.*?(?:(FATAL|ERROR)|WARN|INFO|DEBUG|TRACE)".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val WARN_LOG_LEVEL_RULE = object : ColoringRegexRule(
        "^.*?(?:FATAL|ERROR|(WARN)|INFO|DEBUG|TRACE)".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val INFO_LOG_LEVEL_RULE = object : ColoringRegexRule(
        "^.*?(?:FATAL|ERROR|WARN|(INFO|DEBUG|TRACE))".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val EXTRACT_VALUES_FROM_FIRST_3_BRACKETS_RULE = object : ColoringRegexRule(
        "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)".toRegex()
    ) {
        override fun findFragments(text: String): List<IntRange> {
            return super.findFragments(text).take(3)
        }
    }
    val EXTRACT_VALUES_FROM_SECOND_BRACKETS_RULE = object : ColoringRegexRule(
        "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)".toRegex()
    ) {
        override fun findFragments(text: String): List<IntRange> {
            return super.findFragments(text)
                .getOrNull(1)
                ?.let { listOf(it) }
                ?: listOf()
        }
    }
    val EXTRACT_VALUES_FROM_BRACKETS_RULE = ColoringRegexRule(
        "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)|<([^<>]*)>".toRegex()
    )
    val EXTRACT_VALUES_RULE = ColoringRegexRule(
        "(?<=[,\\s(\\[{])[\\w]+=([\\w+:./?#@-]+)(?=[,\\s)\\]}]|$)".toRegex()
    )
}