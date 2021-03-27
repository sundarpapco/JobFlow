package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class ManagePrintOrderVM : ViewModel() {

    private val _loadedJob = MutableLiveData<LoadingStatus>()
    private val _saveStatus=MutableLiveData<Event<LoadingStatus>>()
    val loadedJob: LiveData<LoadingStatus> = _loadedJob
    val saveStatus:LiveData<Event<LoadingStatus>> = _saveStatus
    private lateinit var printOrder: PrintOrder
    private val repository = Repository()

    /* init{
         createNewJob()
     }*/


    fun loadRepeatJob(plateNumber: Int) {

        if (plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE) {
            createRepeatJob(plateNumber)
        } else
            loadJobFromRepository(plateNumber)
    }

    fun createNewJob() {
        printOrder = PrintOrder()
        _loadedJob.value = LoadingStatus.Success(printOrder)
    }

    fun createRepeatJob(plateNumber: Int) {
        printOrder = PrintOrder()
        printOrder.jobType = PrintOrder.TYPE_REPEAT_JOB
        printOrder.plateMakingDetail.plateNumber = plateNumber
        _loadedJob.value = LoadingStatus.Success(printOrder)
    }

    private fun loadJobFromRepository(plateNumber: Int) {

        viewModelScope.launch {

            _loadedJob.value = LoadingStatus.Loading("One moment please")
            try {
                //Load Job from repository here like
                val searchResult = repository.searchPrintOrderByPlateNumber(plateNumber)
                if (searchResult == null)
                    _loadedJob.value = LoadingStatus.Error(ResourceNotFoundException(""))
                else {
                    printOrder=searchResult
                    printOrder.creationTime= currentTimeInMillis()
                    printOrder.jobType=PrintOrder.TYPE_REPEAT_JOB
                    _loadedJob.value = LoadingStatus.Success(searchResult)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loadedJob.value = LoadingStatus.Error(e)
            }
        }
    }

    fun saveJobDetails(jobName: String, clientName: String) {
        printOrder.jobName = jobName
        printOrder.billingName = clientName
    }

    // Paper details

    fun addPaperDetail(paperDetail: PaperDetail) {
        if (printOrder.paperDetails == null) {
            printOrder.paperDetails = mutableListOf(paperDetail)
        } else {
            /*Create a new Array by copying the old array because the recycler is already holding
            the reference to list in the printOrder and so, the diffUtil will not find any
            difference if we simply add to the already existing list. So, we need to create a new
            list with all existing content, then add the new entry to it and then send that new list
            to the RecyclerView Adapter */

            val newList = ArrayList(printOrder.paperDetails)
            newList.add(paperDetail)
            printOrder.paperDetails = newList
        }

        _loadedJob.value = LoadingStatus.Success(printOrder)
    }

    fun removePaperDetail(index: Int) {

        if (printOrder.paperDetails == null || printOrder.paperDetails!!.size < index + 1) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails)
            newList.removeAt(index)
            printOrder.paperDetails = newList
        }

        _loadedJob.value = LoadingStatus.Success(printOrder)
    }

    fun updatePaperDetail(index: Int, paperDetail: PaperDetail) {

        if (printOrder.paperDetails == null) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails)
            newList[index] = paperDetail
            printOrder.paperDetails = newList
        }

        _loadedJob.value = LoadingStatus.Success(printOrder)
    }

    //Plate making detail
    fun savePlateDetails(details: PlateMakingDetail) {
        printOrder.plateMakingDetail = details
    }

    fun savePrintingDetails(details: PrintingDetail) {
        printOrder.printingDetail = details
    }

    fun savePrintOrder() {

        viewModelScope.launch {
            try {
                _saveStatus.value= loadingEvent("One moment please")
                repository.createPrintOrder(printOrder)
                _saveStatus.value= dataEvent(true)
            } catch (e: Exception) {
                _saveStatus.value= errorEvent(e)
            }
        }

    }


}