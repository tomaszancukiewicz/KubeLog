package com.payu.kube.log.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

internal class PodInfoTest {

    @ParameterizedTest
    @MethodSource("providePodNames")
    fun shouldCalcProperAppName(expected: String, data: PodInfo) {
        Assertions.assertEquals(expected, data.calculatedAppName)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        private fun providePodNames(): List<Arguments> {
            val template = PodInfo(
                "uid", "name", "appNameLabel", "ownerReferenceName", "namespace",
                "containerImage", "containerName", "nodeName", 3, 1, 1, 1,
                PodState.Running, "Running", Instant.now(), Instant.now()
            )

            return listOf(
                Arguments.of(
                    "admin-fronts", template.copy(
                        name = "admin-fronts-69c787bd77-tx2nt", appNameLabel = "admin-fronts",
                        ownerReferencesName = "admin-fronts-69c787bd77", containerName = "admin-fronts"
                    )
                ),
                Arguments.of(
                    "feedzai-simulator", template.copy(
                        name = "feedzai-simulator-584654b4c7-bc292", appNameLabel = "feedzai-simulator",
                        ownerReferencesName = "feedzai-simulator-584654b4c7", containerName = "feedzai-simulator"
                    )
                ),
                Arguments.of(
                    "wallet-gw-rabbitmq", template.copy(
                        name = "wallet-gw-rabbitmq-1", appNameLabel = "",
                        ownerReferencesName = "wallet-gw-rabbitmq", containerName = "rabbitmq"
                    )
                ),
                Arguments.of(
                    "wallet-gw-rabbitmq", template.copy(
                        name = "wallet-gw-rabbitmq-2", appNameLabel = "",
                        ownerReferencesName = "wallet-gw-rabbitmq", containerName = "rabbitmq"
                    )
                ),
                Arguments.of(
                    "redis-slave", template.copy(
                        name = "redis-slave-1", appNameLabel = "",
                        ownerReferencesName = "redis-slave", containerName = "redis"
                    )
                ),
                Arguments.of(
                    "redis-master", template.copy(
                        name = "redis-master-0", appNameLabel = "",
                        ownerReferencesName = "redis-master", containerName = "redis"
                    )
                ),
                Arguments.of(
                    "rabbitmq", template.copy(
                        name = "rabbitmq-0", appNameLabel = "",
                        ownerReferencesName = "rabbitmq", containerName = "rabbitmq"
                    )
                ),
                Arguments.of(
                    "selenium-selenium-chrome-f66949464", template.copy(
                        name = "selenium-selenium-chrome-f66949464-2br4v", appNameLabel = "",
                        ownerReferencesName = "selenium-selenium-chrome-f66949464", containerName = "selenium"
                    )
                ),
                Arguments.of(
                    "selenium-selenium-hub-746bfb7895", template.copy(
                        name = "selenium-selenium-hub-746bfb7895-h2fh5", appNameLabel = "",
                        ownerReferencesName = "selenium-selenium-hub-746bfb7895", containerName = "selenium"
                    )
                )
            )
        }
    }
}