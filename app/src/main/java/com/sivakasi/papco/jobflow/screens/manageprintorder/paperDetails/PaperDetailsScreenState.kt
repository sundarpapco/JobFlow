package com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.data.PaperDetail

class PaperDetailsScreenState(
    val context:Context,
    val isEditMode:Boolean
) {

    var paperDetails:List<PaperDetail> by mutableStateOf(emptyList())
    var paperDialogState:PaperDetailDialogState? by mutableStateOf(null)
        private set

    fun showPaperDetailDialog(
        index:Int,
        paperDetail: PaperDetail
    ){
        paperDialogState = PaperDetailDialogState(context, index,paperDetail)
    }

    fun hidePaperDetailDialog(){
        paperDialogState=null
    }

    fun addPaper(index: Int,paperDetail: PaperDetail){

        val list = paperDetails.toMutableList()
        if(index==-1)   // Meaning new paper added
            paperDetails=list + paperDetail
        else { // Edited an existing paper
            list[index] = paperDetail
            paperDetails=list
        }
    }

    fun removePaper(index: Int){
        val list= paperDetails.toMutableList()
        list.removeAt(index)
        paperDetails=list
    }

    fun validate():Boolean{
        return paperDetails.isNotEmpty()
    }

}