package com.sivakasi.papco.jobflow.util

import androidx.lifecycle.Observer

class Event<T>(val data: T) {

    private var alreadyHandled = false

    fun isAlreadyHandled() = alreadyHandled

    fun handleEvent(): T {
        return if (alreadyHandled)
            throw EventAlreadyHandledException("An event can be handled only once")
        else {
            alreadyHandled = true
            data
        }
    }

    fun peekData() = data
}

class EventObserver<T>(val block:(T)->Unit):Observer<Event<T>>{
    override fun onChanged(t: Event<T>) {
        if(!t.isAlreadyHandled())
            block(t.handleEvent())
    }
}

fun loadingEvent(msg: String): Event<LoadingStatus> =
    Event(LoadingStatus.Loading(msg))

fun <T> dataEvent(data: T): Event<LoadingStatus> =
    Event(LoadingStatus.Success(data))

fun errorEvent(exception: Exception): Event<LoadingStatus> =
    Event(LoadingStatus.Error(exception))