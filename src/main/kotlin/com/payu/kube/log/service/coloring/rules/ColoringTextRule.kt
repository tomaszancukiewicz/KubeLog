package com.payu.kube.log.service.coloring.rules

open class ColoringTextRule(coloringClass: List<String>, private val searchedText: String)
    : ColoringRule(coloringClass) {
    override fun findFragments(text: String): List<IntRange> {
        return findAllIndexes(text, searchedText)
            .map { IntRange(it, it + searchedText.length - 1) }
    }

    private fun findAllIndexes(textString: String, word: String): List<Int> {
        if (word.isEmpty()) {
            return listOf()
        }
        val indexes = mutableListOf<Int>()
        var wordLength = 0
        var index = 0
        while (index != -1) {
            index = textString.indexOf(word, index + wordLength)
            if (index != -1) {
                indexes.add(index)
            }
            wordLength = word.length
        }
        return indexes
    }
}