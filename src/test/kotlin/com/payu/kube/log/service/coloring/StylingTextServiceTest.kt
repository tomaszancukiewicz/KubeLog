package com.payu.kube.log.service.coloring

import com.payu.kube.log.service.coloring.rules.ColoringRegexRule
import com.payu.kube.log.service.coloring.rules.ColoringRule
import com.payu.kube.log.service.coloring.rules.ColoringQueryRule
import com.payu.kube.log.service.search.query.TextQuery
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StylingTextServiceTest {

    private val sut = StylingTextService()

    @ParameterizedTest
    @MethodSource("providerStyledText")
    fun shouldCreateStyledText(text: String, rules: List<ColoringRule>, expected: List<Pair<String, List<String>>>) {
        val result = sut.styleText(text, rules)

        Assertions.assertEquals(text, result.text)
        Assertions.assertEquals(result.text.lastIndex, result.styledStyledSegments.last().range.last)
        Assertions.assertEquals(expected, result.createSegments())
    }

    companion object {
        private const val P_CLASS = "pattern"
        private const val S_CLASS = "searched"

        @Suppress("unused")
        @JvmStatic
        private fun providerStyledText(): List<Arguments> {
            return listOf(
                Arguments.of(
                    "log line",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery(""))
                    ),
                    listOf("log line" to listOf<String>())
                ),

                Arguments.of(
                    "log line",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery("xd"))
                    ),
                    listOf("log line" to listOf<String>())
                ),
                Arguments.of(
                    "abbccbbcc",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery("bb"))
                    ),
                    listOf(
                        "a" to listOf(),
                        "bb" to listOf(S_CLASS),
                        "cc" to listOf(),
                        "bb" to listOf(S_CLASS),
                        "cc" to listOf()
                    )
                ),
                Arguments.of(
                    "abbccbbcc",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery("b"))
                    ),
                    listOf(
                        "a" to listOf(),
                        "bb" to listOf(S_CLASS),
                        "cc" to listOf(),
                        "bb" to listOf(S_CLASS),
                        "cc" to listOf()
                    )
                ),
                Arguments.of(
                    "  123 123  ",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery(""))
                    ),
                    listOf(
                        "  " to listOf(),
                        "123" to listOf(P_CLASS),
                        " " to listOf(),
                        "123" to listOf(P_CLASS),
                        "  " to listOf()
                    )
                ),
                Arguments.of(
                    "  123 123  ",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery("2"))
                    ),
                    listOf(
                        "  " to listOf(),
                        "1" to listOf(P_CLASS),
                        "2" to listOf(P_CLASS, S_CLASS),
                        "3" to listOf(P_CLASS),
                        " " to listOf(),
                        "1" to listOf(P_CLASS),
                        "2" to listOf(P_CLASS, S_CLASS),
                        "3" to listOf(P_CLASS),
                        "  " to listOf()
                    )
                ),
                Arguments.of(
                    "  123  123  ",
                    listOf(
                        ColoringRegexRule(listOf(P_CLASS), "([0-9]+)".toRegex()),
                        ColoringQueryRule(listOf(S_CLASS), TextQuery(" 123 "))
                    ),
                    listOf(
                        " " to listOf(),
                        " " to listOf(S_CLASS),
                        "123" to listOf(P_CLASS, S_CLASS),
                        "  " to listOf(S_CLASS),
                        "123" to listOf(P_CLASS, S_CLASS),
                        " " to listOf(S_CLASS),
                        " " to listOf()
                    )
                ),
                Arguments.of(
                    "10.77.213.151-- - - [28/Feb/2021:16:07:02 +0100] \"GET /index.html HTTP/1.1\" 200 77 \"-\" [repT=261]",
                    listOf(
                        ColoringQueryRule(listOf(S_CLASS), TextQuery("10.77"))
                    ),
                    listOf(
                        "10.77" to listOf(S_CLASS),
                        ".213.151-- - - [28/Feb/2021:16:07:02 +0100] \"GET /index.html HTTP/1.1\" 200 77 \"-\" [repT=261]" to listOf(),
                    )
                )
            )
        }
    }
}