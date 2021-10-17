package com.sivakasi.papco.jobflow.extensions

import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.MainActivity
import com.sivakasi.papco.jobflow.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun Fragment.getActionBar(): ActionBar? {
    return try {
        (requireActivity() as AppCompatActivity).supportActionBar
    } catch (e: Exception) {
        null
    }
}


fun Fragment.updateSubTitle(subTitle: String) {
    getActionBar()?.subtitle = subTitle
}


fun Fragment.updateTitle(title: String) {
    getActionBar()?.title = title
}

fun Fragment.hideActionBar(){
    getActionBar()?.hide()
}

fun Fragment.showActionBar(){
    getActionBar()?.show()
}

fun Fragment.enableBackArrow() {
    setHasOptionsMenu(true)
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
}

fun Fragment.enableBackAsClose() {
    setHasOptionsMenu(true)
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_close)
}

fun Fragment.disableBackArrow() {
    getActionBar()?.setDisplayHomeAsUpEnabled(false)
}

@ExperimentalCoroutinesApi
fun Fragment.currentUserRole(): String =
    (requireActivity() as MainActivity).getUserClaim()

@ExperimentalCoroutinesApi
fun Fragment.saveUserRole(role:String){
    (requireActivity() as MainActivity).saveUserClaim(role)
}

@ExperimentalCoroutinesApi
fun Fragment.signOut(){
    (requireActivity() as MainActivity).logOutUser()
}

fun Fragment.toast(msg:String,length:Int= Toast.LENGTH_SHORT){
    Toast.makeText(requireContext(),msg,length).show()
}

inline fun Fragment.registerBackPressedListener(crossinline block: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        block()
    }
}

