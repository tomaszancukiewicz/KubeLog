package com.kube.log.util

sealed class VirtualItem<T> {
    abstract val originalIndex: Int
}

data class Item<T>(val value: T, val index: Int) : VirtualItem<T>() {
    override val originalIndex: Int
        get() = index
}

data class ShowMoreBeforeItem<T>(val item: Item<T>) : VirtualItem<T>() {
    override val originalIndex: Int
        get() = item.originalIndex
}

data class ShowMoreAfterItem<T>(val item: Item<T>) : VirtualItem<T>() {
    override val originalIndex: Int
        get() = item.originalIndex
}