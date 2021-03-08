package com.payu.kube.log.util

import javafx.scene.control.ListView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

object ClipboardUtils {
    fun copySelectionToClipboard(listView: ListView<*>) {
        val clipboardString = StringBuilder()
        val selectedItems = listView.selectionModel.selectedItems ?: listOf()
        for (item in selectedItems) {
            clipboardString.append(item?.toString())
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