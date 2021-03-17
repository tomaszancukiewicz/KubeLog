package com.payu.kube.log.service.search.query

open class TextQuery(val searchedText: String) : Query() {
    override fun toString(): String {
        return "TextQuery($searchedText)"
    }

    override fun check(text: String): Boolean {
        if (searchedText.isEmpty()) return false
        return searchedText in text
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return findAllIndexes(text)
            .map { IntRange(it, it + searchedText.length - 1) }
    }

    private fun findAllIndexes(textString: String): List<Int> {
        if (searchedText.isEmpty()) {
            return listOf()
        }
        val indexes = mutableListOf<Int>()
        var wordLength = 0
        var index = 0
        while (index != -1) {
            index = textString.indexOf(searchedText, index + wordLength)
            if (index != -1) {
                indexes.add(index)
            }
            wordLength = searchedText.length
        }
        return indexes
    }
}