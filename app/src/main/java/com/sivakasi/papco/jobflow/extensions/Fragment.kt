package com.sivakasi.papco.jobflow.extensions

import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.R

fun Fragment.getActionBar():ActionBar?{
    return try{
        (requireActivity() as AppCompatActivity).supportActionBar
    }catch(e:Exception){
        null
    }
}


fun Fragment.updateSubTitle(subTitle: String) {
    getActionBar()?.subtitle = subTitle
}


fun Fragment.updateTitle(title: String) {
    getActionBar()?.title=title
}

fun Fragment.enableBackArrow(){
    setHasOptionsMenu(true)
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
}

fun Fragment.enableBackAsClose(){
    setHasOptionsMenu(true)
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_close)
}

fun Fragment.disableBackArrow(){
    getActionBar()?.setDisplayHomeAsUpEnabled(false)
}

inline fun Fragment.registerBackPressedListener(crossinline block:()->Unit){
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        block()
    }
}

