package com.kube.log.ui.compose.component.shortcut

import androidx.compose.ui.input.key.*

class Shortcut(private val keys: List<Key>) {
    fun match(event: KeyEvent): Boolean {
        return keys.all {
            when (it) {
                Key.MetaLeft, Key.MetaRight -> event.isMetaPressed
                Key.ShiftLeft, Key.ShiftRight -> event.isShiftPressed
                Key.CtrlLeft, Key.CtrlRight -> event.isCtrlPressed
                Key.AltLeft, Key.AltRight -> event.isAltPressed
                else -> event.key == it
            }
        }
    }

    override fun toString() = keys.joinToString("+") {
        java.awt.event.KeyEvent.getKeyText(it.nativeKeyCode)
    }
}