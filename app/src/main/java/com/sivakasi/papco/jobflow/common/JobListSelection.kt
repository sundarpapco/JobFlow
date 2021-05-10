package com.sivakasi.papco.jobflow.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.util.Duration
import java.util.*
import kotlin.collections.HashMap

class JobListSelection {

    private var selectionDuration = Duration()

    private val selection = HashMap<Int,PrintOrderUIModel>()
    private val _updates = MutableLiveData<Int>()
    val updates: LiveData<Int> = _updates
    private var pendingJobSelectionCount=0

    fun toggle(item: PrintOrderUIModel) {

        if(selection.contains(item.printOrderNumber)){
            selection.remove(item.printOrderNumber)
            selectionDuration-=item.runningTime
            if(item.isPending())
                pendingJobSelectionCount--
        }else{
            selection[item.printOrderNumber]=item
            selectionDuration+=item.runningTime
            if(item.isPending())
                pendingJobSelectionCount++
        }

        _updates.value = selection.size
    }

    fun contains(key:Int):Boolean = selection.contains(key)
    fun size():Int=selection.size

    fun clear() {
        if(selection.size>0) {
            selection.clear()
            selectionDuration=Duration()
            pendingJobSelectionCount=0
            _updates.value=selection.size
        }
    }

    fun hasPendingItems():Boolean =
        pendingJobSelectionCount > 0


    fun title():String="${selection.size} Jobs"
    fun subTitle():String=selectionDuration.toString()

    fun asList(): List<PrintOrderUIModel> = LinkedList<PrintOrderUIModel>().apply {
        addAll(selection.values)
    }

}