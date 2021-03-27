package com.sivakasi.papco.jobflow.data

import android.os.Bundle

data class PrintingDetail(
    var colours:String="4",
    var printingInstructions:String="",
    var runningMinutes:Int=0,
    var hasSpotColours:Boolean=false
){
    companion object{
        private const val KEY_BUNDLE="key:bundle:PrintingDetail"
        private const val KEY_COLOURS="key:colours"
        private const val KEY_INSTRUCTIONS="key:printingInstructions"
        private const val KEY_RUNNING_MINUTES="key:runningMinutes"
        private const val KEY_HAS_SPOT_COLOURS="key:hasSpotColours"

        fun readFromBundle(bundle:Bundle):PrintingDetail?{

            val state= bundle.getBundle(KEY_BUNDLE) ?: return null

            val result=PrintingDetail()
            result.colours=state.getString(KEY_COLOURS) ?: error("colour value not found in bundle")
            result.printingInstructions=state.getString(KEY_INSTRUCTIONS) ?: error("Printing instructions not found in bundle")
            result.runningMinutes=state.getInt(KEY_RUNNING_MINUTES)
            result.hasSpotColours=state.getBoolean(KEY_HAS_SPOT_COLOURS)

            return result
        }
    }

    fun writeToBundle(outState:Bundle){

        val bundle=Bundle().apply {
            putString(KEY_COLOURS,colours)
            putString(KEY_INSTRUCTIONS,printingInstructions)
            putInt(KEY_RUNNING_MINUTES,runningMinutes)
            putBoolean(KEY_HAS_SPOT_COLOURS,hasSpotColours)
        }
        outState.putBundle(KEY_BUNDLE,bundle)
    }
}
