package com.sivakasi.papco.jobflow.common

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.sivakasi.papco.jobflow.util.toast

open class ResultDialogFragment:DialogFragment() {

    companion object{
        private const val KEY_FIRST_RUN="key:firstRun"
    }


    protected var firstRun:Boolean=true

    private val callBack: ResultDialogListener? by lazy{
        getTheCallBack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let{
            firstRun=it.getBoolean(KEY_FIRST_RUN,true)
        }
    }

    override fun onResume() {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_FIRST_RUN,false)
    }

    protected fun dispatchResult(result:Any, code:Int):Boolean{

        return if(callBack==null){
            false
        }else{
            callBack?.onDialogResult(result,code)
            true
        }

    }

    private fun getTheCallBack(): ResultDialogListener? {

        var callback: ResultDialogListener? = null

        try {
            callback = when {
                parentFragment != null -> parentFragment as ResultDialogListener
                activity != null -> activity as ResultDialogListener
                else -> {
                    toast("Should be called either from Activity or fragment")
                    null
                }
            }
        } catch (exception: Exception) {
            toast("Caller should implement ResultDialogListener Interface")
        }

        return callback
    }

    interface ResultDialogListener{
        fun onDialogResult(dialogResult:Any,code:Int)
    }
}