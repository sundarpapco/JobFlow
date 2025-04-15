package com.sivakasi.papco.jobflow.data


class PlateMakingDetail{

    companion object {

        const val PLATE_NUMBER_NOT_YET_ALLOCATED = -1
        const val PLATE_NUMBER_OUTSIDE_PLATE = -2
    }

    var plateNumber: Int = PLATE_NUMBER_NOT_YET_ALLOCATED
    var trimmingHeight: Int = 0
    var trimmingWidth: Int = 0
    var jobHeight: Int = 0
    var jobWidth: Int = 0
    var gripper: Int = 0
    var tail: Int = 0
    var backsidePrinting: String = "None"
    var machine: String = ""
    var screen: String = ""
    var backsideMachine: String = ""

    val trimmingSize: String
        get() = "$trimmingHeight X $trimmingWidth mm"

    val jobSize: String
        get() {
            return if(jobHeight<=0)
                "-"
            else
                "$jobHeight X $jobWidth mm"
        }

    val gripperSize: String
        get() =
            if (gripper > 0)
                "$gripper mm"
            else
                "-"
    val tailSize: String
        get() = if (tail > 0)
            "$tail mm"
        else
            "-"
}