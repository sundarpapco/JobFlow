package com.sivakasi.papco.jobflow.screens.manageprintorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class ManagePrintOrderVM : ViewModel() {

    private val _loadedJob = MutableLiveData<PrintOrder>()
    private val _reprintLoadingStatus= MutableLiveData<Event<LoadingStatus>>()
    private val _saveStatus = MutableLiveData<Event<LoadingStatus>>()

    val loadedJob: LiveData<PrintOrder> = _loadedJob
    val reprintLoadingStatus:LiveData<Event<LoadingStatus>> = _reprintLoadingStatus
    val saveStatus: LiveData<Event<LoadingStatus>> = _saveStatus

    private lateinit var printOrder: PrintOrder
    private val repository = Repository()

    var isEditMode: Boolean = false
    var editingPrintOrderParentDestinationId: String = DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    fun saveLoadedJob(printOrder:PrintOrder){
        this.printOrder=printOrder
        _loadedJob.value=printOrder
    }

    fun loadRepeatJob(plateNumber: Int, searchByPlateNumber: Boolean) {

        if (plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE) {
            createRepeatJob(plateNumber)
        } else
            loadJobFromRepository(plateNumber, searchByPlateNumber)
    }

    fun createNewJob() {
        printOrder = PrintOrder()
        _loadedJob.value = printOrder
    }

    fun createRepeatJob(plateNumber: Int) {
        printOrder = PrintOrder()
        printOrder.jobType = PrintOrder.TYPE_REPEAT_JOB
        printOrder.plateMakingDetail.plateNumber = plateNumber
        _loadedJob.value = printOrder
    }

    private fun loadJobFromRepository(plateNumber: Int, searchByPlateNumber: Boolean) {

        viewModelScope.launch {

            _reprintLoadingStatus.value = loadingEvent("One moment please")
            try {
                //Load Job from repository here like
                val searchResult = if (searchByPlateNumber) {
                    repository.searchPrintOrderByPlateNumber(plateNumber) //Search by plate number
                } else
                    repository.searchPrintOrder(plateNumber) //Search by po number

                if (searchResult == null)
                    _reprintLoadingStatus.value = errorEvent(ResourceNotFoundException(""))
                else {
                    printOrder = searchResult
                    printOrder.prepareForReprint()
                    _reprintLoadingStatus.value= dataEvent(printOrder)
                }
            } catch (e: Exception) {
                _reprintLoadingStatus.value = errorEvent(e)
            }
        }
    }

    fun loadPrintOrderToEdit(poNumber: Int) {
        viewModelScope.launch {

            _reprintLoadingStatus.value = loadingEvent("One moment please")
            try {
                //Load Job from repository here like
                val searchResult = repository.searchPrintOrder(poNumber)
                if (searchResult == null)
                    _reprintLoadingStatus.value = errorEvent(ResourceNotFoundException(""))
                else {
                    printOrder = searchResult
                    _reprintLoadingStatus.value= dataEvent(printOrder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _reprintLoadingStatus.value= errorEvent(e)
            }
        }
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

        _loadedJob.value = printOrder
    }

    fun removePaperDetail(index: Int) {

        if (printOrder.paperDetails == null || printOrder.paperDetails!!.size < index + 1) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails)
            newList.removeAt(index)
            printOrder.paperDetails = newList
        }

        _loadedJob.value = printOrder
    }

    fun updatePaperDetail(index: Int, paperDetail: PaperDetail) {

        if (printOrder.paperDetails == null) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails)
            newList[index] = paperDetail
            printOrder.paperDetails = newList
        }

        _loadedJob.value = printOrder
    }


    fun savePrintingDetails(details: PrintingDetail) {
        printOrder.printingDetail = details
    }

    fun savePrintOrder() {

        viewModelScope.launch {
            try {
                _saveStatus.value = loadingEvent("One moment please")
                repository.createPrintOrder(printOrder)
                _saveStatus.value = dataEvent(true)
            } catch (e: Exception) {
                _saveStatus.value = errorEvent(e)
            }
        }

    }

    fun updatePrintOrder() {

        viewModelScope.launch {
            try {
                _saveStatus.value = loadingEvent("One moment please")
                repository.updatePrintOrder(editingPrintOrderParentDestinationId, printOrder)
                _saveStatus.value = dataEvent(true)
            } catch (e: Exception) {
                _saveStatus.value = errorEvent(e)
            }
        }

    }


}