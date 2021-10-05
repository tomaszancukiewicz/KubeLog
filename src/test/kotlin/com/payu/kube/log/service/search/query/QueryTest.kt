package com.payu.kube.log.service.search.query

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class QueryTest {

    @ParameterizedTest
    @MethodSource("provideQuery")
    fun shouldFindInText(text: String, query: Query, expectedIsMarked: Boolean, markedParts: List<IntRange>) {
        assertEquals(expectedIsMarked, query.check(text))
        assertEquals(markedParts, query.phrasesToMark(text))
    }

    companion object {
        @JvmStatic
        fun provideQuery(): List<Arguments> {
            return listOf(
                Arguments.of(
                    "aabbaabbbc",
                    TextQuery("bb"),
                    true, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    NotQuery(NotQuery(TextQuery("bb"))),
                    true, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    NotQuery(TextQuery("bb")),
                    false, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    AndQuery(TextQuery("bb"), TextQuery("bb")),
                    true, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    OrQuery(TextQuery("bb"), TextQuery("bb")),
                    true, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    AndQuery(TextQuery("bb"), NotQuery(TextQuery("bb"))),
                    false, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    OrQuery(TextQuery("bb"), NotQuery(TextQuery("bb"))),
                    true, listOf(IntRange(2, 3), IntRange(6, 7))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    OrQuery(TextQuery("ab"), TextQuery("ba")),
                    true, listOf(IntRange(1, 6))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    OrQuery(NotQuery(TextQuery("ab")), NotQuery(TextQuery("ba"))),
                    false, listOf(IntRange(1, 6))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    AndQuery(NotQuery(TextQuery("ab")), NotQuery(TextQuery("ba"))),
                    false, listOf(IntRange(1, 6))
                ),
                Arguments.of(
                    "aabbaabbbc",
                    AndQuery(TextQuery("bba"), AndQuery(TextQuery("bb"), TextQuery("bb"))),
                    true, listOf(IntRange(2, 4), IntRange(6, 7))
                ),
            )
        }
    }
}