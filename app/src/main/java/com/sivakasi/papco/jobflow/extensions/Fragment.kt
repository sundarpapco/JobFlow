package com.sivakasi.papco.jobflow.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.updateSubTitle(subTitle: String) {

    try {
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = subTitle
    } catch (e: Exception) {

    }
}

fun Fragment.updateTitle(title: String) {

    try {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    } catch (e: Exception) {

    }
}