package com.payu.kube.log.util

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

object ClipboardUtils {
    fun setClipboardContent(text: String) {
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(text)
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }
}