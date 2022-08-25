package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.screens.manageprintorder.postpress.PostPressScreenState
import com.sivakasi.papco.jobflow.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class ManagePrintOrderVM @Inject constructor(
    private val repository: Repository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val recoveringFromProcessDeath = MutableLiveData(false)

    init {
        val processDeathKey = "process_death_key"
        recoveringFromProcessDeath.value = savedStateHandle.get(processDeathKey) ?: false
        savedStateHandle.set(processDeathKey, true)
    }

    val postPressScreenState = PostPressScreenState(application)

    private val _loadedJob = MutableLiveData<PrintOrder>()
    private val _reprintLoadingStatus = MutableLiveData<Event<LoadingStatus>>()

    val loadedJob: LiveData<PrintOrder> = _loadedJob
    val reprintLoadingStatus: LiveData<Event<LoadingStatus>> = _reprintLoadingStatus

    var saveUpdateStatus: LoadingStatus? by mutableStateOf(null)
    private lateinit var printOrder: PrintOrder

    var isEditMode: Boolean = false
    var editingPrintOrderParentDestinationId: String = DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    fun saveLoadedJob(printOrder: PrintOrder) {
        this.printOrder = printOrder
        _loadedJob.value = printOrder
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

            _reprintLoadingStatus.value =
                loadingEvent(application.getString(R.string.one_moment_please))
            try {
                //Load Job from repository here like
                val searchResult = if (searchByPlateNumber) {
                    repository.getLatestPrintOrderWithPlateNumber(plateNumber) //Search by plate number
                } else
                    repository.fetchPrintOrder(plateNumber) //Search by po number

                if (searchResult == null)
                    _reprintLoadingStatus.value = errorEvent(ResourceNotFoundException(""))
                else {
                    printOrder = searchResult
                    printOrder.prepareForReprint()
                    postPressScreenState.loadPrintOrder(printOrder)
                    _reprintLoadingStatus.value = dataEvent(printOrder)
                }
            } catch (e: Exception) {
                _reprintLoadingStatus.value = errorEvent(e)
            }
        }
    }

    fun loadPrintOrderToEdit(poNumber: Int) {
        viewModelScope.launch {

            _reprintLoadingStatus.value =
                loadingEvent(application.getString(R.string.one_moment_please))
            try {
                //Load Job from repository here like
                val searchResult = repository.fetchPrintOrder(poNumber)
                if (searchResult == null)
                    _reprintLoadingStatus.value = errorEvent(ResourceNotFoundException(""))
                else {
                    printOrder = searchResult
                    postPressScreenState.loadPrintOrder(printOrder)
                    _reprintLoadingStatus.value = dataEvent(printOrder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _reprintLoadingStatus.value = errorEvent(e)
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

            val newList = ArrayList(printOrder.paperDetails!!)
            newList.add(paperDetail)
            printOrder.paperDetails = newList
        }

        _loadedJob.value = printOrder
    }

    fun removePaperDetail(index: Int) {

        if (printOrder.paperDetails == null || printOrder.paperDetails!!.size < index + 1) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails!!)
            newList.removeAt(index)
            printOrder.paperDetails = newList
        }

        _loadedJob.value = printOrder
    }

    fun updatePaperDetail(index: Int, paperDetail: PaperDetail) {

        if (printOrder.paperDetails == null) {
            return
        } else {
            val newList = ArrayList(printOrder.paperDetails!!)
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
                saveUpdateStatus =
                    LoadingStatus.Loading(application.getString(R.string.one_moment_please))
                postPressScreenState.applyToPrintOrder(printOrder)
                repository.createPrintOrder(printOrder)
                saveUpdateStatus = LoadingStatus.Success(Unit)
            } catch (e: Exception) {
                saveUpdateStatus = LoadingStatus.Error(e)
            }
        }

    }

    fun updatePrintOrder() {


        viewModelScope.launch {
            try {
                saveUpdateStatus =
                    LoadingStatus.Loading(application.getString(R.string.one_moment_please))
                postPressScreenState.applyToPrintOrder(printOrder)
                repository.updatePrintOrder(editingPrintOrderParentDestinationId, printOrder)
                saveUpdateStatus = LoadingStatus.Success(Unit)
            } catch (e: Exception) {
                saveUpdateStatus = LoadingStatus.Error(e)
            }
        }
    }
}