package com.payu.kube.log.util

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableObjectValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import com.payu.kube.log.model.PodListState
import java.util.function.Function

object BindingsUtils {
    fun <T> mapToStringBinding(op: ObservableObjectValue<T>?, mapper: Function<T, String?>): StringBinding {
        if (op == null) {
            throw NullPointerException("Operand cannot be null")
        }

        return object : StringBinding() {
            init {
                super.bind(op)
            }

            override fun dispose() {
                super.unbind(op)
            }

            override fun computeValue(): String? {
                return mapper.apply(op.get())
            }

            override fun getDependencies(): ObservableList<*>? {
                return FXCollections.singletonObservableList(op)
            }
        }
    }

    fun <T> ObservableObjectValue<T>.mapToString(mapper: Function<T, String?>): StringBinding {
        return mapToStringBinding(this, mapper)
    }

    fun <T> mapToBooleanBinding(op: ObservableObjectValue<T>?, mapper: Function<T, Boolean>): BooleanBinding {
        if (op == null) {
            throw NullPointerException("Operand cannot be null")
        }

        return object : BooleanBinding() {
            init {
                super.bind(op)
            }

            override fun dispose() {
                super.unbind(op)
            }

            override fun computeValue(): Boolean {
                return mapper.apply(op.get())
            }

            override fun getDependencies(): ObservableList<*>? {
                return FXCollections.singletonObservableList(op)
            }
        }
    }

    fun <T> ObservableObjectValue<T>.mapToBoolean(mapper: Function<T, Boolean>): BooleanBinding {
        return mapToBooleanBinding(this, mapper)
    }

    fun <T, R> mapToObjectBinding(op: ObservableObjectValue<T>?, mapper: Function<T, R>): ObjectBinding<R> {
        if (op == null) {
            throw NullPointerException("Operand cannot be null")
        }

        return object : ObjectBinding<R>() {
            init {
                super.bind(op)
            }

            override fun dispose() {
                super.unbind(op)
            }

            override fun computeValue(): R {
                return mapper.apply(op.get())
            }

            override fun getDependencies(): ObservableList<*>? {
                return FXCollections.singletonObservableList(op)
            }
        }
    }

    fun <T, R> ObservableObjectValue<T>.mapToObject(mapper: Function<T, R>):
            ObjectBinding<R> {
        return mapToObjectBinding(this, mapper)
    }
}