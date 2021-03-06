package com.sivakasi.papco.jobflow.screens.destination

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.JobListSelection
import com.sivakasi.papco.jobflow.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FixedDestinationVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    val jobSelections = JobListSelection(application)
    private var isAlreadyLoaded = false
    private val _loadedJobs = MutableLiveData<LoadingStatus>()
    private val _destination = MutableLiveData<Destination>()
    private val _workingStatus = MutableLiveData<Event<LoadingStatus>>()
    val loadedJobs: LiveData<LoadingStatus> = _loadedJobs
    val destination: LiveData<Destination> = _destination
    val workingStatus: LiveData<Event<LoadingStatus>> = _workingStatus


    private fun observeDestination(destinationId: String) {
        viewModelScope.launch {
            try {
                repository.observeDestination(destinationId)
                    .collect {
                        _destination.value = it ?: Destination(name = destinationId)
                    }
            } catch (e: Exception) {
                //Toast the error message or send a signal to UI
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
        _loadedJobs.value = LoadingStatus.Loading("")
        viewModelScope.launch {
            try {
                repository.jobsOfDestination(destinationId)
                    .collect {
                        _loadedJobs.value = LoadingStatus.Success(it)
                    }
            } catch (e: Exception) {
                _loadedJobs.value = LoadingStatus.Error(e)
            }
        }
    }

    fun updateJobs(destinationId: String, jobs: List<PrintOrderUIModel>) {
        viewModelScope.launch {
            repository.batchUpdateJobs(destinationId, jobs)
        }
    }

    fun cancelSelectedJobs(sourceId: String) {
        allotSelectedJobs(sourceId, DatabaseContract.DOCUMENT_DEST_CANCELLED)
    }

    fun allotSelectedJobs(sourceId: String, destinationId: String) {
        val jobs = jobSelections.asList()
        doWork { repository.moveJobs(sourceId, destinationId, jobs) }
    }

    fun invoiceSelectedJob(sourceId: String, invoiceDetail: String) {
        val jobs = jobSelections.asList()
        doWork {
            val time = currentTimeInMillis()
            repository.moveJobs(
                sourceId,
                DatabaseContract.DOCUMENT_DEST_COMPLETED,
                jobs
            ) {
                it.invoiceDetails = invoiceDetail.trim()
                it.completionTime = time
            }
        }
    }

    fun markSelectedJobsAsComplete(sourceId: String, completionTime: Long) {
        val jobs = jobSelections.asList()
        doWork {
            repository.moveJobs(
                sourceId,
                DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                jobs
            ) {
                it.completionTime = completionTime
            }
        }
    }

    fun backtrackSelectedJobs(sourceId: String) {
        val jobs = jobSelections.asList()
        doWork { repository.backtrackJobs(sourceId, jobs) }
    }

    fun clearPendingStatus(destinationId: String, item: PrintOrderUIModel) =
        doWork { repository.clearPendingStatus(destinationId, listOf(item)) }

    fun clearPendingStatusOfSelectedItems(destinationId: String) {
        val jobs = jobSelections.asList()
        doWork { repository.clearPendingStatus(destinationId, jobs) }
    }

    fun markAsPending(destinationId: String, remark: String) {
        val jobs = jobSelections.asList()
        doWork {
            repository.markAsPending(destinationId, remark, jobs)
        }
    }


    private inline fun doWork(crossinline block: suspend () -> Boolean) {
        viewModelScope.launch {
            try {
                _workingStatus.value =
                    loadingEvent(application.getString(R.string.one_moment_please))
                val result=block()
                _workingStatus.value = dataEvent(result)
            } catch (e: Exception) {
                _workingStatus.value = errorEvent(e)
            }
        }
    }

}