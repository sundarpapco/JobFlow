package com.sivakasi.papco.jobflow.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.sivakasi.papco.jobflow.BuildConfig

fun isPrinterVersionApp():Boolean =
    BuildConfig.FLAVOR == "printer"

fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
