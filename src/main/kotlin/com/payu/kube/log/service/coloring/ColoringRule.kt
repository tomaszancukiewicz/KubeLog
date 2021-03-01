package com.payu.kube.log.service.coloring

abstract class ColoringRule(val coloringClass: String) {
    abstract fun findFragments(text: String): List<IntRange>

    class ColoringTextRule(coloringClass: String, private val searchedText: String) : ColoringRule(coloringClass) {
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

    class ColoringRegexRule(coloringClass: String, private val regex: Regex) : ColoringRule(coloringClass) {
        override fun findFragments(text: String): List<IntRange> {
            return regex.findAll(text)
                .map { matchResult ->
                    matchResult
                        .takeIf { it.groups.size > 1 }
                        ?.groups
                        ?.get(1)?.range
                        ?: matchResult.range
                }
                .toList()
        }
    }
}