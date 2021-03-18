package com.payu.kube.log.service.search.query

abstract class BinaryOperationQuery(val q1: Query, val q2: Query) : Query() {
    override fun toString(): String {
        return "BinaryOperationQuery($q1, $q2, errors=$errors)"
    }

    override fun hashCode(): Int {
        return 31 * (31 * super.hashCode() + q1.hashCode()) + q2.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other))
            return false
        if (other !is BinaryOperationQuery)
            return false
        return q1 == other.q1 && q2 == other.q2
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        val ranges1 = q1.phrasesToMark(text)
        val ranges2 = q2.phrasesToMark(text)
        val allRanges = (ranges1 + ranges2).sortedBy { it.first }
        val result = mutableListOf<IntRange>()
        for (range in allRanges) {
            val lastRange = result.lastOrNull()
            if (lastRange == null || lastRange.last + 1 < range.first) {
                result.add(range)
            } else if (lastRange.last <= range.first && lastRange.last < range.last) {
                result[result.lastIndex] = IntRange(lastRange.first, range.last)
            }
        }
        return result.toList()
    }
}