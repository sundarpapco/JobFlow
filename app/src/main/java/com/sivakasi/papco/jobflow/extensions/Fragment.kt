package com.sivakasi.papco.jobflow.extensions

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
}

fun Fragment.enableBackAsClose() {
    getActionBar()?.setDisplayHomeAsUpEnabled(true)
    getActionBar()?.setHomeAsUpIndicator(R.drawable.ic_close)
}

fun Fragment.disableBackArrow() {
    getActionBar()?.setDisplayHomeAsUpEnabled(false)
}

fun Fragment.registerBackArrowMenu(
    onBack:()->Unit ={
        findNavController().popBackStack()
    }
){
    val menuProvider = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == android.R.id.home) {
                onBack()
                return true
            }

            return false
        }
    }

    requireActivity().addMenuProvider(menuProvider,viewLifecycleOwner)
}

@ExperimentalCoroutinesApi
fun Fragment.currentUserRole(): String =
    (requireActivity() as MainActivity).getUserClaim()


fun Fragment.toast(msg:String,length:Int= Toast.LENGTH_SHORT){
    Toast.makeText(requireContext(),msg,length).show()
}

inline fun Fragment.registerBackPressedListener(crossinline block: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        block()
    }
}

