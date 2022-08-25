package com.sivakasi.papco.jobflow.common

import android.app.Application
import androidx.lifecycle.LiveData
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.util.Duration
import java.util.*

class JobListSelection(
    private val application: Application
) : LiveData<Int>() {

    private var selectionDuration = Duration()
    private val selection = HashMap<Int, PrintOrderUIModel>()
    private var pendingJobSelectionCount = 0

    /*
    This selection can be locked by setting the locked to true. If locked, then toggling or clearing
    the selection wont have any effect and those calls will be simply ignored until the lock is
    turned off again.
     */
    var locked = false

    fun toggle(item: PrintOrderUIModel) {

        if(locked)
            return

        if (selection.contains(item.printOrderNumber)) {
            selection.remove(item.printOrderNumber)
            selectionDuration -= item.runningTime
            if (item.isPending())
                pendingJobSelectionCount--
        } else {
            selection[item.printOrderNumber] = item
            selectionDuration += item.runningTime
            if (item.isPending())
                pendingJobSelectionCount++
        }

        value = selection.size
    }

    fun contains(key: Int): Boolean = selection.contains(key)
    fun size(): Int = selection.size

    fun clear() {

        if(locked)
            return

        if (selection.size > 0) {
            selection.clear()
            selectionDuration = Duration()
            pendingJobSelectionCount = 0
            value = selection.size
        }
    }

    fun hasPendingItems(): Boolean =
        pendingJobSelectionCount > 0


    //Will be used to disable the Invoice option in the menu when more than one customer is selected
    fun hasMultipleCustomersSelected():Boolean{

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


    fun title(): String = application.getString(R.string.xx_jobs,selection.size)
    fun subTitle(): String = selectionDuration.toString()

    fun asList(): List<PrintOrderUIModel> = LinkedList<PrintOrderUIModel>().apply {
        addAll(selection.values)
    }
}