package com.sivakasi.papco.jobflow.data

data class Foil(
    var material:Int= MATERIAL_GOLD,
    var remarks:String=""
){
    companion object{
        const val MATERIAL_GOLD=0
        const val MATERIAL_SILVER=1
    }
}
