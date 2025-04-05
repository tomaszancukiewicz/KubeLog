package com.payu.kube.log.service.search

import com.payu.kube.log.service.search.query.*
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
                    "\"abc\"",
                    TextQuery("abc")
                ),
                Arguments.of(
                    "abc",
                    TextQuery("abc")
                ),
                Arguments.of(
                    "\"abc\" OR NOT('123')",
                    OrQuery(
                        TextQuery("abc"),
                        NotQuery(TextQuery("123"))
                    )
                ),
                Arguments.of(
                    "abc OR NOT 123",
                    OrQuery(
                        TextQuery("abc"),
                        NotQuery(TextQuery("123"))
                    )
                ),
                Arguments.of(
                    "\"1\" or not '2' and '3'",
                    AndQuery(
                        OrQuery(
                            TextQuery("1"),
                            NotQuery(TextQuery("2")),
                        ),
                        TextQuery("3")
                    )
                ),
                Arguments.of(
                    "\"1\" OR ((NOT '2') AND '3')",
                    OrQuery(
                        TextQuery("1"),
                        AndQuery(
                            NotQuery(TextQuery("2")),
                            TextQuery("3")
                        ),
                    )
                ),
                Arguments.of(
                    "1 or ((not 2) and 3)",
                    OrQuery(
                        TextQuery("1"),
                        AndQuery(
                            NotQuery(TextQuery("2")),
                            TextQuery("3")
                        ),
                    )
                )
            )
        }
    }
}