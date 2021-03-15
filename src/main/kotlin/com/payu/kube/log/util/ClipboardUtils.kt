package com.payu.kube.log.util

import com.payu.kube.log.ui.tab.list.LogListView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

object ClipboardUtils {
    fun copySelectionToClipboard(listView: LogListView) {
        val clipboardString = StringBuilder()
        val selectedItems = listView.selectionModel.selectedItems ?: listOf()
        for (item in selectedItems) {
            clipboardString.append(item?.text)
            clipboardString.append("\n")
        }
        setClipboardContent(clipboardString.toString())
    }

    fun setClipboardContent(text: String) {
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(text)
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }
}