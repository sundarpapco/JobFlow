package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.app.Application
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val recoveringFromProcessDeath = MutableStateFlow(false)

    init {
        val processDeathKey = "process_death_key"
        recoveringFromProcessDeath.value = savedStateHandle[processDeathKey] ?: false
        savedStateHandle[processDeathKey] = true
    }

    private val _loadedJob:MutableStateFlow<PrintOrder?> = MutableStateFlow(null)
    val loadedJob = _loadedJob.asStateFlow()

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
        loadPrintOrderToScreens(printOrder)
        printOrder.plateMakingDetail.plateNumber = plateNumber
        plateMakingDetailsScreenState.plateNumber = plateNumber
        _loadedJob.value = printOrder
    }

    fun loadJobByPlateNumber(plateNumber: Int) {

        viewModelScope.launch {

            addJobScreenState.showWaiting()
            try {
                //Load Job from repository here like
                val searchResult =
                    repository.getLatestPrintOrderWithPlateNumber(plateNumber) //Search by plate number

                if (searchResult == null)
                    addJobScreenState.showIsPONotFoundDialog()
                else {
                    printOrder = searchResult
                    //Must be a reprint because Editing a Job is not possible with Plate number.
                    //So, always prepare for Reprint
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

    fun loadJobByPONumber(poNumber: Int) {

        viewModelScope.launch {
            addJobScreenState.showWaiting()
            try {
                //Load Job from repository here like
                val searchResult = repository.fetchPrintOrder(poNumber)
                if (searchResult == null)
                    addJobScreenState.showIsPONotFoundDialog()
                else {
                    printOrder = searchResult
                    //Print Order can be loaded for two different purposes with PO Number
                    //For reprint via AutoRepeat or for editing with Edit FAB Button
                    //So, Check & Prepare for Reprint only when loaded via AutoReprint
                    if (!isEditMode)
                        printOrder.prepareForReprint()
                    loadPrintOrderToScreens(printOrder)
                    _loadedJob.value = printOrder
                }
            } catch (e: Exception) {
                e.printStackTrace()
                application.toastError(e)
            }
        }
    }

    private fun loadPrintOrderToScreens(printOrder: PrintOrder) {
        jobDetailsScreenState.loadPrintOrder(
            printOrder,
            editingPrintOrderParentDestinationId,
            isEditMode
        )
        paperDetailsScreenState.loadPrintOrder(printOrder, isEditMode)
        plateMakingDetailsScreenState.loadPrintOrder(printOrder, isEditMode)
        printingDetailsScreenState.loadPrintOrder(printOrder, isEditMode)
        postPressScreenState.loadPrintOrder(printOrder, isEditMode)
    }

    private fun saveScreensToPrintOrder(printOrder: PrintOrder) {
        jobDetailsScreenState.saveToPrintOrder(printOrder)
        paperDetailsScreenState.saveToPrintOrder(printOrder)
        plateMakingDetailsScreenState.saveToPrintOrder(printOrder)
        printingDetailsScreenState.saveToPrintOrder(printOrder)
        postPressScreenState.saveToPrintOrder(printOrder)
    }

    fun createPrintOrder() {

        viewModelScope.launch {
            try {
                postPressScreenState.showLoadingStatus()
                saveScreensToPrintOrder(printOrder)
                repository.createPrintOrder(printOrder)
                postPressScreenState.loadingSuccessStatus("Success")
            } catch (e: Exception) {
                postPressScreenState.loadingErrorStatus(e)
            }
        }

    }

    fun updatePrintOrder() {

        viewModelScope.launch {
            try {
                postPressScreenState.showLoadingStatus()
                saveScreensToPrintOrder(printOrder)
                repository.updatePrintOrder(editingPrintOrderParentDestinationId, printOrder)
                postPressScreenState.loadingSuccessStatus("Success")
            } catch (e: Exception) {
                postPressScreenState.loadingErrorStatus(e)
            }
        }
    }
}