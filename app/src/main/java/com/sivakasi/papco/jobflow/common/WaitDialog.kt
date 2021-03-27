package com.sivakasi.papco.jobflow.common

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

class WaitDialog:DialogFragment() {

    companion object{

        const val KEY_MESSAGE="Key_for_message"
        const val DEFAULT_MESSAGE="Please wait..."
        const val TAG="papco.sundar:waitDialog"

        fun getInstance(msg:String): WaitDialog {
            val args=Bundle()
            args.putString(KEY_MESSAGE,msg)
            return WaitDialog().also {it.arguments=args}
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog=ProgressDialog(requireActivity())
        progressDialog.isIndeterminate=true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage(getMessage())
        progressDialog.setCancelable(false)
        isCancelable=false
        return progressDialog
    }

    fun setMessage(msg:String){
        arguments?.putString(KEY_MESSAGE,msg)
        (dialog as ProgressDialog).setMessage(msg)
    }


    private fun getMessage():String = arguments?.getString(KEY_MESSAGE) ?: DEFAULT_MESSAGE

}

fun Fragment.showWaitDialog(msg: String) {
    WaitDialog.getInstance(msg)
        .show(childFragmentManager, WaitDialog.TAG)
    childFragmentManager.executePendingTransactions()
}

fun Fragment.hideWaitDialog() {
    val showingDialog = childFragmentManager.findFragmentByTag(WaitDialog.TAG)
    showingDialog?.let {
        (it as WaitDialog).dismiss()
    }
}

fun Fragment.updateWaitDialogMessage(msg: String) {
    val showingDialog = childFragmentManager.findFragmentByTag(WaitDialog.TAG)
    showingDialog?.let {
        (it as WaitDialog).setMessage(msg)
    }
}