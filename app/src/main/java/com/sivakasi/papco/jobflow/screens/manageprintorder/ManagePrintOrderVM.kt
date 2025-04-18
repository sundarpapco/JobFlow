package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.screens.manageprintorder.addJob.AddJobScreenState
import com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails.JobDetailsScreenState
import com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails.PaperDetailsScreenState
import com.sivakasi.papco.jobflow.screens.manageprintorder.plateMakingDetails.PlateMakingDetailsScreenState
import com.sivakasi.papco.jobflow.screens.manageprintorder.postpress.PostPressScreenState
import com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails.PrintingDetailsScreenState
import com.sivakasi.papco.jobflow.util.Event
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.dataEvent
import com.sivakasi.papco.jobflow.util.errorEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class ManagePrintOrderVM @Inject constructor(
    private val repository: Repository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val addJobScreenState = AddJobScreenState()
    val jobDetailsScreenState = JobDetailsScreenState(application)
    val paperDetailsScreenState = PaperDetailsScreenState(application)
    val plateMakingDetailsScreenState = PlateMakingDetailsScreenState(application)
    val printingDetailsScreenState = PrintingDetailsScreenState(application)
    val postPressScreenState = PostPressScreenState(application)

    val recoveringFromProcessDeath = MutableLiveData(false)

    init {
        val processDeathKey = "process_death_key"
        recoveringFromProcessDeath.value = savedStateHandle[processDeathKey] ?: false
        savedStateHandle[processDeathKey] = true
    }

    private val _loadedJob = MutableLiveData<PrintOrder>()
    val loadedJob: LiveData<PrintOrder> = _loadedJob

    var saveUpdateStatus:MutableStateFlow<Event<LoadingStatus>?> = MutableStateFlow(null)
    private lateinit var printOrder: PrintOrder

    var isEditMode: Boolean = false
    var editingPrintOrderParentDestinationId: String = DatabaseContract.DOCUMENT_DEST_NEW_JOBS


    fun createNewJob() {
        printOrder = PrintOrder()
        _loadedJob.value = printOrder
    }

    fun createRepeatJob(plateNumber: Int) {
        printOrder = PrintOrder()
        printOrder.jobType = PrintOrder.TYPE_REPEAT_JOB
        printOrder.plateMakingDetail.plateNumber = plateNumber
        plateMakingDetailsScreenState.plateNumber=plateNumber
        _loadedJob.value = printOrder
    }

    fun loadJobFromRepository(plateNumber: Int) {

        viewModelScope.launch {

            addJobScreenState.showWaiting()
            try {
                //Load Job from repository here like
                val searchResult=repository.getLatestPrintOrderWithPlateNumber(plateNumber) //Search by plate number

                if (searchResult == null)
                    addJobScreenState.showIsPONotFoundDialog()
                else {
                    printOrder = searchResult
                    printOrder.prepareForReprint()
                    loadPrintOrderToScreens(printOrder)
                    _loadedJob.value = printOrder
                }
            } catch (e: Exception) {
                addJobScreenState.hideWaiting()
                application.toastError(e)
            }
        }
    }

    fun loadPrintOrderToEdit(poNumber: Int) {

        viewModelScope.launch {
            addJobScreenState.showWaiting()
            try {
                //Load Job from repository here like
                val searchResult = repository.fetchPrintOrder(poNumber)
                if (searchResult == null)
                    addJobScreenState.showIsPONotFoundDialog()
                else {
                    printOrder = searchResult
                    loadPrintOrderToScreens(printOrder)
                    _loadedJob.value = printOrder
                }
            } catch (e: Exception) {
                e.printStackTrace()
                application.toastError(e)
            }
        }
    }

    private fun loadPrintOrderToScreens(printOrder: PrintOrder){
        jobDetailsScreenState.loadPrintOrder(printOrder,editingPrintOrderParentDestinationId,isEditMode)
        paperDetailsScreenState.loadPrintOrder(printOrder,isEditMode)
        plateMakingDetailsScreenState.loadPrintOrder(printOrder,isEditMode)
        printingDetailsScreenState.loadPrintOrder(printOrder,isEditMode)
        postPressScreenState.loadPrintOrder(printOrder,isEditMode)
    }

    private fun saveScreensToPrintOrder(printOrder: PrintOrder){
        jobDetailsScreenState.saveToPrintOrder(printOrder)
        paperDetailsScreenState.saveToPrintOrder(printOrder)
        plateMakingDetailsScreenState.saveToPrintOrder(printOrder)
        printingDetailsScreenState.saveToPrintOrder(printOrder)
        postPressScreenState.saveToPrintOrder(printOrder)
    }

    fun savePrintOrder() {

        Log.d("SAATVIK","Saving Print Order")

        viewModelScope.launch {
            try {
                postPressScreenState.showWaitDialog()
               saveScreensToPrintOrder(printOrder)
                Log.d("SAATVIK","Plate Number: ${printOrder.plateMakingDetail.plateNumber}")
                Log.d("SAATVIK","Job Type: ${printOrder.jobType}")
                //repository.createPrintOrder(printOrder)
                delay(2000)
                postPressScreenState.hideWaitDialog()
                saveUpdateStatus.value = dataEvent("Success")
            } catch (e: Exception) {
                postPressScreenState.hideWaitDialog()
                saveUpdateStatus.value = errorEvent(e)
            }
        }

    }

    fun updatePrintOrder() {

        Log.d("SAATVIK","Updating Print Order")

        viewModelScope.launch {
            try {
                postPressScreenState.showWaitDialog()
                saveScreensToPrintOrder(printOrder)
                Log.d("SAATVIK","Plate Number: ${printOrder.plateMakingDetail.plateNumber}")
                Log.d("SAATVIK","Job Type: ${printOrder.jobType}")
                //repository.updatePrintOrder(editingPrintOrderParentDestinationId, printOrder)
                delay(2000)
                postPressScreenState.hideWaitDialog()
                saveUpdateStatus.value = dataEvent("Success")
            } catch (e: Exception) {
                postPressScreenState.hideWaitDialog()
                saveUpdateStatus.value = errorEvent(e)
            }
        }
    }
}