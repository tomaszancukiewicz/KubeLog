package com.kube.log.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerUtils {
    inline fun <reified R : Any> R.logger(): Logger {
        if (R::class.isCompanion) {
            return LoggerFactory.getLogger(this.javaClass.enclosingClass)!!
        }
        return LoggerFactory.getLogger(this.javaClass)!!
    }
}