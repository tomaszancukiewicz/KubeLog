package com.kube.log.service.coloring.rules

abstract class ColoringRule {
    abstract fun findFragments(text: String): List<IntRange>
}