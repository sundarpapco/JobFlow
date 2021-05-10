package com.sivakasi.papco.jobflow.common

import com.sivakasi.papco.jobflow.models.PrintOrderUIModel

interface JobsAdapterListener {
    fun onItemClick(item:PrintOrderUIModel, position:Int)
    fun onItemLongClick(item:PrintOrderUIModel, position:Int){}
    fun onItemMoved(updatingJobs:List<PrintOrderUIModel>){}
    fun showPendingRemarks(item:PrintOrderUIModel)
}