package com.payu.kube.log.service.coloring

import com.payu.kube.log.service.coloring.rules.ColoringRule
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class StylingTextService {

    fun calcSegmentForIndex(text: String, rules: List<ColoringRule>, index: Int): String? {
        for (rule in rules.reversed()) {
            val fragments = rule.findFragments(text)
            val range = fragments.firstOrNull { it.contains(index) } ?: continue
            return text.substring(range)
        }
        return null
    }

    fun styleText(text: String, rules: List<ColoringRule>): StyledText {
        return styleText(StyledText(text), rules)
    }

    fun styleText(styledText: StyledText, rules: List<ColoringRule>): StyledText {
        val appliedStyles = styledText.appliedStyles.toMutableSet()
        var segments = styledText.styledStyledSegments
        for (rule in rules) {
            val fragments = rule.findFragments(styledText.text)
            if (fragments.isNotEmpty()) {
                segments = merge(segments, rule.coloringClass, fragments)
                appliedStyles.addAll(rule.coloringClass)
            }
        }
        val calcSegments = mergeSimilarSegments(segments)
        return StyledText(styledText.text, calcSegments, appliedStyles)
    }

    private fun mergeSimilarSegments(segments: List<StyledSegment>): List<StyledSegment> {
        return segments.fold(mutableListOf()) { acc, segment ->
            val lastSegment = acc.lastOrNull()
            if (lastSegment?.styles == segment.styles) {
                lastSegment.range = IntRange(lastSegment.range.first, segment.range.last)
            } else {
                acc.add(segment)
            }
            acc
        }
    }

    private fun merge(
        styledSegments: List<StyledSegment>, styles: List<String>, fragments: List<IntRange>
    ): List<StyledSegment> {
        var iS = 0
        var iF = 0
        var index = 0
        val results = mutableListOf<StyledSegment>()
        while (iS < styledSegments.size && iF < fragments.size) {
            val styledSegmentRange = styledSegments[iS]
            val fragmentRange = fragments[iF]

            val newStyledSegment = if (index < fragmentRange.first) {
                val end = min(fragmentRange.first - 1, styledSegmentRange.range.last)
                StyledSegment(styledSegmentRange.styles, IntRange(index, end))
            } else {
                val end = min(fragmentRange.last, styledSegmentRange.range.last)
                val appliedStyles = styledSegmentRange.styles.toMutableList()
                styles.forEach { style ->
                    appliedStyles.remove(style)
                    appliedStyles.add(style)
                }
                StyledSegment(appliedStyles, IntRange(index, end))
            }

            results.add(newStyledSegment)
            index = newStyledSegment.range.last + 1

            if (index > styledSegmentRange.range.last) {
                iS += 1
            }
            if (index > fragmentRange.last) {
                iF += 1
            }
        }
        while (iS < styledSegments.size) {
            val fragment = styledSegments[iS]
            results.add(fragment.copy(range = IntRange(index, fragment.range.last)))
            index = fragment.range.last + 1
            iS += 1
        }
        return results
    }
}