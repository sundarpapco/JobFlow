package com.sivakasi.papco.jobflow.screens.manageprintorder.plateMakingDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.extensions.intNumber

class PlateMakingDetailsScreenState(
    private val context: Context
) {
    var isEditMode by mutableStateOf(false)
    var plateNumber by  mutableIntStateOf( PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED)
    var dontCheckSize by mutableStateOf(false)
    var trimHeight by mutableStateOf(TextFieldValue(""))
    var trimHeightError: String? by mutableStateOf(null)
    var trimWidth by mutableStateOf(TextFieldValue(""))
    var trimWidthError: String? by mutableStateOf(null)
    var jobHeight by mutableStateOf(TextFieldValue(""))
    var jobWidth by mutableStateOf(TextFieldValue(""))
    var gripper by mutableStateOf(TextFieldValue(""))
    var tail by mutableStateOf(TextFieldValue(""))
    var machine by mutableStateOf(TextFieldValue(""))
    var machineError: String? by mutableStateOf(null)
    var screen by mutableStateOf(TextFieldValue(""))
    var screenError:String? by mutableStateOf(null)
    var backside by mutableStateOf("None")
    var backsideMachine by mutableStateOf(TextFieldValue(""))

    fun loadPlateMakingDetail(plateMakingDetail: PlateMakingDetail) {

        isEditMode = true
        plateNumber = plateMakingDetail.plateNumber

        trimHeight = if (plateMakingDetail.trimmingHeight == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.trimmingHeight.toString())

        trimWidth = if (plateMakingDetail.trimmingWidth == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.trimmingWidth.toString())

        jobHeight = if (plateMakingDetail.jobHeight == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.jobHeight.toString())

        jobWidth = if (plateMakingDetail.jobWidth == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.jobWidth.toString())

        gripper = if (plateMakingDetail.gripper == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.gripper.toString())

        tail = if (plateMakingDetail.tail == 0)
            TextFieldValue("")
        else
            TextFieldValue(plateMakingDetail.tail.toString())

        machine = TextFieldValue(plateMakingDetail.machine)
        screen = TextFieldValue(plateMakingDetail.screen)
        backside = plateMakingDetail.backsidePrinting
        backsideMachine = TextFieldValue(plateMakingDetail.backsideMachine)
    }

    fun autoSetGripperAndTail(){
        val trimHeightNumber = trimHeight.text.intNumber(0)
        val jobHeightNumber = jobHeight.text.intNumber(0)
        val space =trimHeightNumber-jobHeightNumber
        if(space > 10){
            gripper = TextFieldValue("10")
            tail = TextFieldValue((space-10).toString())
        }else{
            gripper = TextFieldValue(space.toString())
            tail=TextFieldValue("0")
        }

        if(space < 0){
            gripper = TextFieldValue("")
            tail = TextFieldValue("")
        }
    }

    fun validate(): Boolean {

        var valid = true
        val trimHeightNumber = trimHeight.text.intNumber(0)
        val trimWidthNumber = trimWidth.text.intNumber(0)
        val gripperNumber = gripper.text.intNumber(0)
        val tailNumber = tail.text.intNumber(0)
        val jobHeightNumber = jobHeight.text.intNumber(0)
        val jobWidthNumber = jobWidth.text.intNumber(0)

        //Since these 2 field errors can be cleared by altering the other fields
        //also, we should clear that here first
        trimHeightError = null
        trimWidthError = null

        if (!dontCheckSize) {

            if (trimHeightNumber !in 360..720) {
                trimHeightError = context.getString(R.string.error_invalid_trim_size, 340, 720)
                valid = false
            }

            if (trimWidthNumber !in 540..1020) {
                trimWidthError = context.getString(R.string.error_invalid_trim_size, 540, 1020)
                valid = false
            }

            if(plateNumber != PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE){
                if(jobHeightNumber+gripperNumber+tailNumber != trimHeightNumber){
                    trimHeightError=context.getString(R.string.error_invalid)
                    valid=false
                }

                if( jobWidthNumber > trimWidthNumber){
                    trimWidthError=context.getString(R.string.error_invalid)
                    valid=false
                }

                if(screen.text.isBlank()){
                    screenError=context.getString(R.string.required_field)
                    valid=false
                }
            }
        }

        if (machine.text.isBlank()) {
            machineError = context.getString(R.string.required_field)
            valid = false
        }

        return valid

    }

    fun toPlateMakingDetail():PlateMakingDetail{

       val plateDetails = PlateMakingDetail()
        plateDetails.trimmingHeight=trimHeight.text.intNumber(0)
        plateDetails.trimmingWidth=trimWidth.text.intNumber(0)

        if(plateNumber != PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE) {
            plateDetails.jobHeight = jobHeight.text.intNumber(0)
            plateDetails.jobWidth = jobWidth.text.intNumber(0)
            plateDetails.gripper = gripper.text.intNumber(0)
            plateDetails.tail=tail.text.intNumber(0)
            plateDetails.screen = screen.text.trim()
        }else{
            plateDetails.jobHeight = 0
            plateDetails.jobWidth = 0
            plateDetails.gripper = 0
            plateDetails.tail=0
            plateDetails.screen=""
        }

        plateDetails.machine = machine.text.trim()
        plateDetails.backsidePrinting = backside.trim()
        plateDetails.backsideMachine=backsideMachine.text.trim()

        return plateDetails

    }

}