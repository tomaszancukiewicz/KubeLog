package com.kube.log.service.search.query

class TextQuery(val searchedText: String, private val ignoreCase: Boolean = true) : Query() {
    override fun toString(): String {
        val options = buildSet {
            if (ignoreCase) add(RegexOption.IGNORE_CASE)
        }
        return "TextQuery($searchedText, $options)"
    }

    override fun hashCode(): Int {
        return 31 * (31 * super.hashCode() + searchedText.hashCode()) + ignoreCase.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other)) return false
        if (other !is TextQuery) return false
        return searchedText == other.searchedText && ignoreCase == other.ignoreCase
    }

    override fun check(text: String): Boolean {
        if (searchedText.isEmpty()) return false
        return text.contains(searchedText, ignoreCase)
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return findAllIndexes(text, ignoreCase)
            .map { IntRange(it, it + searchedText.length - 1) }
    }

    private fun findAllIndexes(textString: String, ignoreCase: Boolean): List<Int> {
        if (searchedText.isEmpty()) return listOf()
        val indexes = mutableListOf<Int>()
        var wordLength = 0
        var index = 0
        while (index != -1) {
            index = textString.indexOf(searchedText, index + wordLength, ignoreCase)
            if (index != -1) {
                indexes.add(index)
            }
            wordLength = searchedText.length
        }
        return indexes
    }
}