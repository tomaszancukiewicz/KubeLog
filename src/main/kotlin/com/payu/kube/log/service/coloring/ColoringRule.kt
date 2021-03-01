package com.payu.kube.log.service.coloring

abstract class ColoringRule(val coloringClass: List<String>) {
    abstract fun findFragments(text: String): List<IntRange>

    class ColoringTextRule(coloringClass: List<String>, private val searchedText: String) : ColoringRule(coloringClass) {
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

    class ColoringRegexRule(
        coloringClass: List<String>, private val regex: Regex, private val chooseGroup: Int? = null
    ) : ColoringRule
        (coloringClass) {
        override fun findFragments(text: String): List<IntRange> {
            return regex.findAll(text)
                .map { matchResult ->
                    if (chooseGroup != null) {
                        return@map matchResult.groups
                            .takeIf { chooseGroup < it.size }
                            ?.get(chooseGroup)
                            ?.range
                    }
                    var matchGroup: MatchGroup? = null
                    for (i in 1 until matchResult.groups.size) {
                        val group = matchResult.groups[1]
                        if (group != null) {
                            matchGroup = group
                            break
                        }
                    }
                    matchGroup?.range ?: matchResult.range
                }
                .filterNotNull()
                .toList()
        }
    }
}