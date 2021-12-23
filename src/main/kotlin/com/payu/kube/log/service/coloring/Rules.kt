package com.payu.kube.log.service.coloring

import com.payu.kube.log.service.coloring.rules.ColoringRegexRule
import com.payu.kube.log.util.RegexUtils.getOrNull

object Rules {
    const val COLORED_GREEN_TEXT_CLASS = "color-background-green"
    const val COLORED_YELLOW_TEXT_CLASS = "color-background-yellow"
    const val COLORED_RED_TEXT_CLASS = "color-background-red"
    const val COLORED_BLUE_TEXT_CLASS = "color-background-blue"
    const val COLORED_GRAY_TEXT_CLASS = "color-background-gray"
    const val COLORED_PURPLE_TEXT_CLASS = "color-background-purple"

    val HTTP_METHODS_RULE = ColoringRegexRule(
        listOf(COLORED_BLUE_TEXT_CLASS, "http-method"),
        "(?<=[^a-z-A-Z])(GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)(?=[^a-z-A-Z])".toRegex()
    )
    val ERROR_LOG_LEVEL_RULE = object : ColoringRegexRule(
        listOf(COLORED_RED_TEXT_CLASS, "log-level"),
        "^.*?(?:(FATAL|ERROR)|WARN|INFO|DEBUG|TRACE)".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val WARN_LOG_LEVEL_RULE = object : ColoringRegexRule(
        listOf(COLORED_YELLOW_TEXT_CLASS, "log-level"),
        "^.*?(?:FATAL|ERROR|(WARN)|INFO|DEBUG|TRACE)".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val INFO_LOG_LEVEL_RULE = object : ColoringRegexRule(
        listOf(COLORED_GREEN_TEXT_CLASS, "log-level"),
        "^.*?(?:FATAL|ERROR|WARN|(INFO|DEBUG|TRACE))".toRegex()
    ) {
        override fun extractRange(matchResult: MatchResult): IntRange? {
            return matchResult.groups
                .getOrNull(1)
                ?.range
        }
    }
    val EXTRACT_VALUES_FROM_FIRST_3_BRACKETS_RULE = object : ColoringRegexRule(
        listOf(COLORED_BLUE_TEXT_CLASS, "brackets"),
        "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)".toRegex()
    ) {
        override fun findFragments(text: String): List<IntRange> {
            return super.findFragments(text).take(3)
        }
    }
    val EXTRACT_VALUES_FROM_SECOND_BRACKETS_RULE = object : ColoringRegexRule(
        listOf(COLORED_PURPLE_TEXT_CLASS, "brackets"),
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
        listOf("brackets"),
        "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)|<([^<>]*)>".toRegex()
    )
    val EXTRACT_VALUES_RULE = ColoringRegexRule(
        listOf(COLORED_GRAY_TEXT_CLASS, "value"),
        "(?<=[,\\s(\\[{])[\\w]+=([\\w+:./?#@-]+)(?=[,\\s)\\]}]|$)".toRegex()
    )
    val IP_RULE = ColoringRegexRule(
        listOf(COLORED_GRAY_TEXT_CLASS, "ip"),
        "(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})){3}".toRegex()
    )
    val EMAIL_RULE = ColoringRegexRule(
        listOf(COLORED_GRAY_TEXT_CLASS, "email"),
        "(?:[\\w]+(?:[.+-][\\w]+)*)@(?:(?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.(?:[a-z]{2,6}(?:\\.[a-z]{2})?)".toRegex()
    )
    val QQ_ID_RULE = ColoringRegexRule(
        listOf(COLORED_GRAY_TEXT_CLASS, "qq-id"),
        "QQ[0-9A-Z]{16}QQ|TT[0-9]{10}TT".toRegex()
    )
}