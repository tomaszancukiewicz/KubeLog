package com.kube.log.model

sealed class PodState {
    abstract fun short(): String
    abstract fun long(): String

    data class Waiting(val reason: String? = null) : PodState() {
        override fun short(): String {
            if (reason.isNullOrBlank()) {
                return "Waiting"
            }
            return reason
        }

        override fun long(): String {
            return "Waiting - $reason"
        }
    }

    data class Terminated(val reason: String, val exitCode: Int, val finishedAt: String) : PodState() {
        override fun short(): String {
            return "$reason($exitCode)"
        }

        override fun long(): String {
            return "Terminated - $reason($exitCode)"
        }
    }

    object Running : PodState() {
        override fun short(): String {
            return "Running"
        }

        override fun long(): String {
            return "Running"
        }
    }
}