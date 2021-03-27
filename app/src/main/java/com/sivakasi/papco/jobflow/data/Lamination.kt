package com.sivakasi.papco.jobflow.data

data class Lamination(
    var material:Int= MATERIAL_PVC,
    var micron:Int=7,
    var remarks:String=""
){
    companion object{
        const val MATERIAL_PVC=0
        const val MATERIAL_BOPP=1
        const val MATERIAL_MATT=2
    }

    override fun toString(): String {
        return "$micron Micron ${laminationName()} "
    }

    private fun laminationName():String{

        return when(material){

            MATERIAL_PVC -> "PVC"
            MATERIAL_BOPP-> "BOPP"
            MATERIAL_MATT -> "Matt"
            else->throw IllegalStateException("Invalid Lamination material name")
        }

    }
}
