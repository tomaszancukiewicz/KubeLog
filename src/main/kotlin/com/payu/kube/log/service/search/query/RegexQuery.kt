package com.payu.kube.log.service.search.query

class RegexQuery(val regex: Regex) : Query() {
    override fun toString(): String {
        return "RegexQuery($regex)"
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + regex.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other))
            return false
        if (other !is RegexQuery)
            return false
        return regex == other.regex
    }

    private fun createRegexWith(ignoreCase: Boolean): Regex {
        return if (ignoreCase) {
            Regex(regex.pattern, RegexOption.IGNORE_CASE)
        } else {
            regex
        }
    }

    override fun check(text: String, ignoreCase: Boolean): Boolean {
        if (regex.pattern.isEmpty()) return false
        return createRegexWith(ignoreCase).containsMatchIn(text)
    }

    override fun phrasesToMark(text: String, ignoreCase: Boolean): List<IntRange> {
        return createRegexWith(ignoreCase).findAll(text).map { it.range }.toList()
    }

    override fun toQueryString(): String {
        val escapedPattern = regex.pattern.replace("\"", "\\\"")
        return "r\"$escapedPattern\""
    }
}