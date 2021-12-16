package com.payu.kube.log.ui.tab.list

sealed class VirtualItem<T> {
    abstract var originalIndex: Int
}

data class Item<T>(val value : T, var index: Int) : VirtualItem<T>() {
    override var originalIndex: Int
        get() = index
        set(value) { index = value }
}

data class ShowMoreBeforeItem<T>(val item: Item<T>) : VirtualItem<T>() {
    override var originalIndex: Int
        get() = item.originalIndex
        set(value) { item.originalIndex = value }
}

data class ShowMoreAfterItem<T>(val item: Item<T>) : VirtualItem<T>() {
    override var originalIndex: Int
        get() = item.originalIndex
        set(value) { item.originalIndex = value }
}