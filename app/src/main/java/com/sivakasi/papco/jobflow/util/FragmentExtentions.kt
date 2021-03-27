package com.sivakasi.papco.jobflow.util

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(msg:String,length:Int= Toast.LENGTH_SHORT){
    Toast.makeText(requireContext(),msg,length).show()
}