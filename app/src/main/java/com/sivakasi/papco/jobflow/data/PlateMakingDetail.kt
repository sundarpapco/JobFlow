package com.sivakasi.papco.jobflow.data

class PlateMakingDetail {

    companion object{
        const val PLATE_NUMBER_NOT_YET_ALLOCATED=-1
        const val PLATE_NUMBER_OUTSIDE_PLATE=-2
    }

    var plateNumber:Int= PLATE_NUMBER_NOT_YET_ALLOCATED
    var trimmingHeight:Int=-1
    var trimmingWidth:Int=-1
    var jobHeight:Int=-1
    var jobWidth:Int=-1
    var gripper:Int=-1
    var tail:Int=-1
    var backsidePrinting:String="None"
    var machine:String=""
    var screen:String=""
    var backsideMachine:String=""
}