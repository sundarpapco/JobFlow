package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.print.PrintOrderReport
import com.sivakasi.papco.jobflow.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class ComposeViewPrintOrderFragmentVM @Inject constructor(
    private val repository: Repository,
    private val application: Application,
    val printOrderReport: PrintOrderReport
) : ViewModel() {


    private var isAlreadyLoaded = false
    private var loadedPrintOrder: PrintOrder? = null
    val screenState = ViewPrintOrderScreenState()


    fun loadPrintOrder(printOrderNumber: Int, userRole: String) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {

            repository.observePrintOrder(printOrderNumber)
                .catch {
                    val e = it as? Exception ?: Exception(it)
                    screenState.loadError(application, e)
                }
                .collect { printOrderWithDestination ->
                    if (printOrderWithDestination == null) {
                        loadedPrintOrder = null
                        screenState.printOrderMoved()
                    } else {
                        loadedPrintOrder = printOrderWithDestination.printOrder
                        screenState.loadPrintOrder(
                            application,
                            printOrderWithDestination,
                            userRole
                        )
                        screenState.destinationName =
                            printOrderWithDestination.destination.name
                    }
                }

        }
    }

    fun revokePrintOrder(printOrder: PrintOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                screenState.showWaitDialog()
                repository.moveJobs(
                    DatabaseContract.DOCUMENT_DEST_COMPLETED,
                    DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                    listOf(PrintOrderUIModel.fromPrintOrder(printOrder))
                ) {
                    it.prepareForRevoking()
                }
                screenState.hideWaitDialog()
            } catch (e: Exception) {
                screenState.hideWaitDialog()
                screenState.toastError = Event(
                    e.message ?: application.getString(R.string.error_unknown_error)
                )
            }
        }
    }


}