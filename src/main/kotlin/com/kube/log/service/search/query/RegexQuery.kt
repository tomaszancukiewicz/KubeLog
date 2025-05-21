package com.kube.log.service.search.query

class RegexQuery(val regex: Regex) : Query() {
    override fun toString(): String {
        return "RegexQuery(${regex.pattern}, ${regex.options})"
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + regex.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other)) return false
        if (other !is RegexQuery) return false
        return regex.pattern == other.regex.pattern && regex.options == other.regex.options
    }

    override fun check(text: String): Boolean {
        if (regex.pattern.isEmpty()) return false
        return regex.containsMatchIn(text)
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return regex.findAll(text).map { it.range }.toList()
    }
}