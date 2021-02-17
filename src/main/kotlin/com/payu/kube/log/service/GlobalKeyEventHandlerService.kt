package com.payu.kube.log.service

import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import org.springframework.stereotype.Service

@Service
class GlobalKeyEventHandlerService {
    private val eventHandlers = mutableListOf<EventHandler<KeyEvent>>()

    fun onKeyPressed(event: KeyEvent) {
        val handlers = eventHandlers.toList()
        for (handler in handlers) {
            if (!event.isConsumed) {
                handler.handle(event)
            }
        }
    }

    fun registerKeyPressEventHandler(handler: EventHandler<KeyEvent>) {
        eventHandlers.add(handler)
    }

    fun unregisterKeyPressEventHandler(handler: EventHandler<KeyEvent>) {
        eventHandlers.remove(handler)
    }
}