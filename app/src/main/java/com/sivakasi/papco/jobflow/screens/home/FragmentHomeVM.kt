package com.sivakasi.papco.jobflow.screens.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FragmentHomeVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private val _newJobs = MutableLiveData<Destination>()
    private val _inProgress = MutableLiveData<Destination>()
    private val _machines = MutableLiveData<Destination>()
    val newJobs: LiveData<Destination> = _newJobs
    val inProgress: LiveData<Destination> = _inProgress
    val machines: LiveData<Destination> = _machines

    init {
        observeNewJobs()
        observeInProgress()
        observeDynamicDestinations()
    }

    private fun observeNewJobs() {
        viewModelScope.launch {
            try {
                repository.observeDestination(DatabaseContract.DOCUMENT_DEST_NEW_JOBS)
                    .collect { destination ->
                        if (destination == null) {
                            _newJobs.value =
                                emptyDestination(DatabaseContract.DOCUMENT_DEST_NEW_JOBS)
                        } else
                            _newJobs.value = destination
                    }
            } catch (e: Exception) {
                _newJobs.value = emptyDestination(DatabaseContract.DOCUMENT_DEST_NEW_JOBS)
            }
        }
    }

    private fun observeInProgress() {
        viewModelScope.launch {
            try {
                repository.observeDestination(DatabaseContract.DOCUMENT_DEST_IN_PROGRESS)
                    .collect { destination ->
                        if (destination == null) {
                            _inProgress.value =
                                emptyDestination(DatabaseContract.DOCUMENT_DEST_IN_PROGRESS)
                        } else
                            _inProgress.value = destination
                    }
            } catch (e: Exception) {
                _inProgress.value = emptyDestination(DatabaseContract.DOCUMENT_DEST_IN_PROGRESS)
            }
        }
    }

    private fun observeDynamicDestinations() {
        viewModelScope.launch {
            try {
                repository.loadAllMachines()
                    .map {
                        if (it.isNotEmpty())
                            it.reduce { acc, destination ->
                                acc.jobCount += destination.jobCount
                                acc.runningTime += destination.runningTime
                                acc
                            }
                        else
                            emptyDestination(application.getString(R.string.machines))
                    }.collect {
                        it.name = application.getString(R.string.machines)
                        _machines.value = it
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                _machines.value = emptyDestination(application.getString(R.string.machines))
            }
        }

    }

    private fun emptyDestination(destinationName: String): Destination =
        Destination().apply {
            name = destinationName
            type = Destination.TYPE_FIXED
            timeBased = true
        }

}