package com.sivakasi.papco.jobflow.screens.destination

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.ProcessingHistory
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.currentTimeInMillis
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FixedDestinationVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private var isAlreadyLoaded = false
    val screenState = DestinationScreenState(application)

    private fun observeDestination(destinationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.observeDestination(destinationId)
                    .collect {
                        screenState.destination = it ?: Destination(name = destinationId)
                    }
            } catch (e: Exception) {
                screenState.workError(e)
            }
        }
    }

    fun loadJobsFromDestination(destinationId: String) {
        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true
        observeDestination(destinationId)
        triggerJobsLoading(destinationId)
    }

    private fun triggerJobsLoading(destinationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.jobsOfDestination(destinationId)
                    .collect {
                        screenState.loadJobs(it)
                    }
            } catch (e: Exception) {
                screenState.workError(e)
            }
        }
    }


    fun updateJobs(destinationId: String, jobs: List<PrintOrderUIModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.batchUpdateJobs(destinationId, jobs)
        }
    }

    fun cancelSelectedJobs(sourceId: String) {
        val jobs = screenState.selection.asList()
        val time = currentTimeInMillis()
        doWork {
            repository.moveJobs(sourceId, DatabaseContract.DOCUMENT_DEST_CANCELLED, jobs) {
                it.completionTime = time
                it.processingHistory += ProcessingHistory(
                    DatabaseContract.DOCUMENT_DEST_CANCELLED,
                    DatabaseContract.DOCUMENT_DEST_CANCELLED,
                    time
                )
            }
        }
    }

    fun allotSelectedJobs(sourceId: String, destinationId: String) {
        val jobs = screenState.selection.asList()
        doWork { repository.moveJobs(sourceId, destinationId, jobs) }
    }

    fun invoiceSelectedJob(sourceId: String, invoiceDetail: String) {

        val jobs = screenState.selection.asList()
        doWork {
            /*Only jobs from the same customer can be Invoiced together. Make sure all the jobs are from
            the same customer before actually invoicing*/
            val customerId = jobs.first().clientId
            jobs.forEach {
                if (it.clientId != customerId)
                    throw IllegalStateException(application.getString(R.string.error_invoicing_multiple_clients))
            }

            repository.moveJobs(
                sourceId,
                DatabaseContract.DOCUMENT_DEST_COMPLETED,
                jobs
            ) {
                it.prepareForInvoicing(invoiceDetail)
            }
        }
    }

    fun partDispatchSelectedJob(sourceId: String, invoiceDetail: String) {
        val jobs = screenState.selection.asList()
        doWork {
            repository.partDispatchJobs(
                sourceId,
                jobs,
                invoiceDetail
            )
        }
    }

    fun markSelectedJobsAsComplete(sourceId: String) {
        val jobs = screenState.selection.asList()
        if (jobs.isEmpty())
            return

        val processingDestination = screenState.destination
        doWork {
            repository.moveJobs(
                sourceId,
                DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                jobs
            ) {
                it.addProcessingHistory(processingDestination)
            }
        }
    }

    fun backtrackSelectedJobs(sourceId: String) {
        val jobs = screenState.selection.asList()
        doWork { repository.backtrackJobs(sourceId, jobs) }
    }

    //Clear the pending status of a single job or the selected Jobs
    //If the item is passed, then only the status of that item will be cleared
    //If its null, then all the selected items will be cleared
    fun clearPendingStatus(destinationId: String, item: PrintOrderUIModel?) {
        val jobs = item?.let { listOf(it) } ?: screenState.selection.asList()
        doWork { repository.clearPendingStatus(destinationId, jobs) }
    }

    fun markAsPending(destinationId: String, remark: String) {
        val jobs = screenState.selection.asList()
        doWork {
            repository.markAsPending(destinationId, remark, jobs)
        }
    }


    private inline fun doWork(crossinline block: suspend () -> Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                screenState.startWorking()
                block()
                screenState.workCompleted()
            } catch (e: Exception) {
                screenState.workError(e)
            }
        }
    }

}