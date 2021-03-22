package com.payu.kube.log.ui.tab.list

import com.payu.kube.log.service.coloring.StyledText

sealed class VirtualItem {
    abstract var originalIndex: Int
}

data class Item(val value : StyledText, var index: Int) : VirtualItem() {
    override var originalIndex: Int
        get() = index
        set(value) { index = value }
}

data class ShowMoreBeforeItem(val item: Item) : VirtualItem() {
    override var originalIndex: Int
        get() = item.originalIndex
        set(value) { item.originalIndex = value }
}

data class ShowMoreAfterItem(val item: Item) : VirtualItem() {
    override var originalIndex: Int
        get() = item.originalIndex
        set(value) { item.originalIndex = value }
}