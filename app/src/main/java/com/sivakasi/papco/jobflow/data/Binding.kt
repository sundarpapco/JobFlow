package com.sivakasi.papco.jobflow.data

import android.content.Context
import com.sivakasi.papco.jobflow.R

data class Binding(
    var type:Int=0,
    var remarks:String=""
){
    companion object{
        const val TYPE_SADDLE_STITCH=0
        const val TYPE_PERFECT=1
        const val TYPE_CASE=2
    }

    fun getBindingName(context:Context):String{
        return when(type){
            TYPE_SADDLE_STITCH->context.getString(R.string.saddle_stitched)
            TYPE_PERFECT->context.getString(R.string.perfect_binding)
            TYPE_CASE->context.getString(R.string.case_binding)
            else-> error("Invalid binding type found in binding object")
        }
    }
}