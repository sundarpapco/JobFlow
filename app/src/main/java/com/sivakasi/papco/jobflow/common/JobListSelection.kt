package com.sivakasi.papco.jobflow.common

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.util.Duration
import java.util.LinkedList

class JobListSelection(
    private val context: Context
) {

    var selectionCount by mutableIntStateOf(0)
    private set

    var showInvoiceOption by mutableStateOf(true)
    private set

    var pendingJobSelectionCount by mutableIntStateOf( 0)
    private set

    private var selectionDuration = Duration()
    private val selection = HashMap<Int, PrintOrderUIModel>()


    /*
    This function will be called whenever a newList is about to be loaded to the UI
    This Function makes sure that all the jobs in the selection are still in the newly arrived
    list. If in case its not found, then, it will be removed from the selection
     */
    fun assertList(jobs:List<PrintOrderUIModel>){
        val jobsToRemoveFromSelection = HashSet<PrintOrderUIModel>()
        var found:Boolean
        for(entry in selection){
            found = false
            loop@ for(job in jobs){
                if(entry.value.printOrderNumber == job.printOrderNumber) {
                    found=true
                    break@loop
                }
            }
            if(!found)
                jobsToRemoveFromSelection.add(entry.value)
        }

        jobsToRemoveFromSelection.forEach {
            remove(it)
        }
    }

    fun toggle(item: PrintOrderUIModel) {
        if (selection.contains(item.printOrderNumber)) {
            remove(item)
        } else {
            add(item)
        }
    }

    private fun add(item:PrintOrderUIModel){
        selection[item.printOrderNumber] = item
        selectionDuration += item.runningTime
        if (item.isPending())
            pendingJobSelectionCount++
        selectionCount=selection.size
        showInvoiceOption=!hasMultipleCustomersSelected()
    }

    private fun remove(item: PrintOrderUIModel){
        selection.remove(item.printOrderNumber)
        selectionDuration -= item.runningTime
        if (item.isPending())
            pendingJobSelectionCount--
        selectionCount=selection.size
        showInvoiceOption=!hasMultipleCustomersSelected()
    }

    fun contains(key: Int): Boolean = selection.contains(key)

    fun clear() {
        if (selection.size > 0) {
            selection.clear()
            selectionDuration = Duration()
            pendingJobSelectionCount = 0
            selectionCount = selection.size
        }
    }


    //Will be used to disable the Invoice option in the menu when more than one customer is selected
    private fun hasMultipleCustomersSelected():Boolean{

        if(selection.size==0)
            return false

        var customerId = -2
        for(entry in selection){
            if(customerId==-2)
                customerId=entry.value.clientId
            else{
                if(customerId!=entry.value.clientId)
                    return true
            }
        }
        return false
    }


    fun title(): String = context.getString(R.string.xx_jobs,selectionCount)
    fun subTitle(): String = selectionDuration.toString()

    fun asList(): List<PrintOrderUIModel> = LinkedList<PrintOrderUIModel>().apply {
        addAll(selection.values)
    }
}