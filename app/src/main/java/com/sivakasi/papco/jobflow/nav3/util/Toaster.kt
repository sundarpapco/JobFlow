package com.sivakasi.papco.jobflow.nav3.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sivakasi.papco.jobflow.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed class ToastMessage {
    class Message(val message: String) : ToastMessage()
    class Resource(@StringRes val resourceId: Int) : ToastMessage()
}

open class ToasterState {
    private val _toaster = Channel<ToastMessage>()
    val toaster = _toaster.receiveAsFlow()

    fun toastError(e: Exception) {
        e.message?.let {
            _toaster.trySend(ToastMessage.Message(it))
        } ?: _toaster.trySend(ToastMessage.Resource(R.string.unknown_error))
    }

    fun toastMessage(message: String) {
        _toaster.trySend(ToastMessage.Message(message))
    }

    fun toastResource(resourceId: Int) {
        _toaster.trySend(ToastMessage.Resource(resourceId))
    }
}

@Composable
fun Toaster(context: Context, toasterState: ToasterState) {
    LaunchedEffect(Unit) {
        toasterState.toaster.collect { toast ->
            val message = when (toast) {
                is ToastMessage.Message -> {
                    toast.message
                }

                is ToastMessage.Resource -> {
                    context.getString(toast.resourceId)
                }
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}