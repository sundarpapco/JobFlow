package com.sivakasi.papco.jobflow.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.util.Duration
import kotlinx.coroutines.selects.select
import java.util.*
import kotlin.collections.HashMap

class JobListSelection {

    private var selectionDuration = Duration()

    private val selection = HashMap<Int,PrintOrderUIModel>()
    private val _updates = MutableLiveData<Int>()
    val updates: LiveData<Int> = _updates

    fun toggle(item: PrintOrderUIModel) {

        if(selection.contains(item.printOrderNumber)){
            selection.remove(item.printOrderNumber)
            selectionDuration-=item.runningTime
        }else{
            selection[item.printOrderNumber]=item
            selectionDuration+=item.runningTime
        }

        _updates.value = selection.size
    }

    fun add(item: PrintOrderUIModel) {
        if(!selection.contains(item.printOrderNumber)){
            selection[item.printOrderNumber]=item
            selectionDuration+=item.runningTime
            _updates.value = selection.size
        }
    }

    fun remove(item: PrintOrderUIModel) {
        if(selection.contains(item.printOrderNumber)){
            selection.remove(item.printOrderNumber)
            selectionDuration-=item.runningTime
            _updates.value = selection.size
        }
    }

    fun contains(key:Int):Boolean = selection.contains(key)
    fun size():Int=selection.size

    fun clear() {
        if(selection.size>0) {
            selection.clear()
            selectionDuration=Duration()
            _updates.value=selection.size
        }
    }


    fun title():String="${selection.size} Jobs"
    fun subTitle():String=selectionDuration.toString()

    fun asList(): List<PrintOrderUIModel> = LinkedList<PrintOrderUIModel>().apply {
        addAll(selection.values)
    }

}