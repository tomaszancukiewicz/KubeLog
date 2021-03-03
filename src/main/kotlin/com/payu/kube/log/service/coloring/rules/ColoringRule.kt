package com.payu.kube.log.service.coloring.rules

abstract class ColoringRule(val coloringClass: List<String>) {
    abstract fun findFragments(text: String): List<IntRange>
}