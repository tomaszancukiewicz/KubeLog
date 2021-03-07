package com.payu.kube.log.view

import javafx.application.Platform
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.skin.ListViewSkin
import javafx.scene.control.skin.VirtualFlow
import java.util.concurrent.Executors

class CustomListViewSkin<T>(control: ListView<T>) : ListViewSkin<T>(control) {
    override fun createVirtualFlow(): VirtualFlow<ListCell<T>> {
        return CustomVirtualFlow()
    }

    private fun getCustomFlow(): CustomVirtualFlow<T> {
        return virtualFlow as CustomVirtualFlow<T>
    }

    fun forceScrollTo(index: Int) {
        if (index in 0 until itemCount) {
            getCustomFlow().forceScrollTo(index)
        }
    }

    class CustomVirtualFlow<T> : VirtualFlow<ListCell<T>>() {
        private val executor = Executors.newSingleThreadExecutor {
            Executors.defaultThreadFactory()
                .newThread(it).apply {
                    isDaemon = true
                }
        }

        fun forceScrollTo(index: Int) {
            executor.execute {
                while(shouldTryScrollOneMoreTime(index)) {
                    Platform.runLater {
                        scrollToTop(index)
                    }
                    Thread.sleep(10)
                }
            }
        }

        private fun shouldTryScrollOneMoreTime(index: Int): Boolean {
            if (index !in 0 until cellCount)
                return false
            return runCatching { getVisibleCell(index) == null }
                .getOrNull() ?: false
        }
    }
}