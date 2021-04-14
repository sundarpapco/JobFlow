package com.sivakasi.papco.jobflow.screens.destination

import androidx.lifecycle.*
import com.sivakasi.papco.jobflow.common.JobListSelection
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
    private val savedStateHandle: SavedStateHandle,
    private val repository: Repository
) : ViewModel() {

    val jobSelections = JobListSelection()
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
        viewModelScope.launch {
            try {
                _workingStatus.value = loadingEvent("One moment please")
                repository.moveJobs(sourceId, destinationId, jobs)
                _workingStatus.value = dataEvent(true)
            } catch (e: Exception) {
                _workingStatus.value = errorEvent(e)
            }
        }
    }

    fun invoiceSelectedJob(sourceId: String, invoiceDetail: String) {
        val jobs = jobSelections.asList()
        viewModelScope.launch {
            try {
                _workingStatus.value = loadingEvent("One moment please")
                repository.moveJobs(
                    sourceId,
                    DatabaseContract.DOCUMENT_DEST_COMPLETED,
                    jobs){
                    it.invoiceDetails=invoiceDetail
                }
                _workingStatus.value = dataEvent(true)
            } catch (e: Exception) {
                _workingStatus.value = errorEvent(e)
            }
        }
    }

    fun markSelectedJobsAsComplete(sourceId: String,completionTime:Long){
        val jobs=jobSelections.asList()
        viewModelScope.launch {
            try{
                _workingStatus.value= loadingEvent("One moment please")
                repository.moveJobs(
                    sourceId,
                    DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                    jobs
                ){
                    it.completionTime=completionTime
                }
                _workingStatus.value = dataEvent(true)
            }catch (e:Exception){
                _workingStatus.value = errorEvent(e)
            }
        }
    }

    fun backtrackSelectedJobs(sourceId: String){
        val jobs=jobSelections.asList()
        viewModelScope.launch {
            try{
                _workingStatus.value= loadingEvent("One moment please")
                repository.backtrackJobs(sourceId,jobs)
                _workingStatus.value = dataEvent(true)
            }catch (e:Exception){
                _workingStatus.value = errorEvent(e)
            }
        }
    }

}