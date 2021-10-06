package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import com.sivakasi.papco.jobflow.R

fun Exception.getMessage(
    context:Context
):String = message ?: context.getString(R.string.error_unknown_error)