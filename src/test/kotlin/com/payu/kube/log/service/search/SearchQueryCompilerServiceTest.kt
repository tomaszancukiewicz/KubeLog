package com.kube.log.service.search

import com.kube.log.service.search.query.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SearchQueryCompilerServiceTest {

    @ParameterizedTest
    @MethodSource("provideQuery")
    fun shouldCompileQuery(text: String, expectedQuery: Query) {
        val compileQuery = SearchQueryCompilerService.compile(text)

        assertLinesMatch(listOf(), compileQuery.errors)
        assertEquals(expectedQuery, compileQuery.query)
    }

    companion object {
        @JvmStatic
        fun provideQuery(): List<Arguments> {
            return listOf(
                Arguments.of(
                    "abc",
                    TextQuery("abc", true)
                ),
                Arguments.of(
                    "\"abc\"",
                    TextQuery("abc", false)
                ),
                Arguments.of(
                    "'abc'",
                    TextQuery("abc", false)
                ),
                Arguments.of(
                    "ri\"abc\"",
                    RegexQuery("abc".toRegex(RegexOption.IGNORE_CASE))
                ),
                Arguments.of(
                    "not '123'",
                    NotQuery(TextQuery("123", false))
                ),
                Arguments.of(
                    "NOT '123'",
                    NotQuery(TextQuery("123", false))
                ),
                Arguments.of(
                    "'123' '234'",
                    AndQuery(
                        TextQuery("123", false),
                        TextQuery("234", false),
                    )
                ),
                Arguments.of(
                    "'123' and '234'",
                    AndQuery(
                        TextQuery("123", false),
                        TextQuery("234", false),
                    )
                ),
                Arguments.of(
                    "'123' AND '234'",
                    AndQuery(
                        TextQuery("123", false),
                        TextQuery("234", false),
                    )
                ),
                Arguments.of(
                    "'123' or '234'",
                    OrQuery(
                        TextQuery("123", false),
                        TextQuery("234", false),
                    )
                ),
                Arguments.of(
                    "'123' OR '234'",
                    OrQuery(
                        TextQuery("123", false),
                        TextQuery("234", false),
                    )
                ),
                Arguments.of(
                    "\"abc\" OR NOT('123')",
                    OrQuery(
                        TextQuery("abc", false),
                        NotQuery(TextQuery("123", false))
                    )
                ),
                Arguments.of(
                    "abc or not 123",
                    OrQuery(
                        TextQuery("abc", true),
                        NotQuery(TextQuery("123", true))
                    )
                ),
                Arguments.of(
                    "1 or not 2 and '3'",
                    OrQuery(
                        TextQuery("1", true),
                        AndQuery(
                            NotQuery(TextQuery("2", true)),
                            TextQuery("3", false)
                        ),
                    )
                ),
                Arguments.of(
                    "1 or ((not 2) and '3')",
                    OrQuery(
                        TextQuery("1", true),
                        AndQuery(
                            NotQuery(TextQuery("2", true)),
                            TextQuery("3", false)
                        ),
                    )
                ),
                Arguments.of(
                    "(1 or not 2) and 3",
                    AndQuery(
                        OrQuery(
                            TextQuery("1", true),
                            NotQuery(TextQuery("2", true)),
                        ),
                        TextQuery("3", true)
                    )
                ),
                Arguments.of(
                    "a b or c d",
                    OrQuery(
                        AndQuery(
                            TextQuery("a", true),
                            TextQuery("b", true),
                        ),
                        AndQuery(
                            TextQuery("c", true),
                            TextQuery("d", true),
                        ),
                    )
                ),
            )
        }
    }
}