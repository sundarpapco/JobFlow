package com.sivakasi.papco.jobflow.screens.home

import android.app.Application
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.Duration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@HiltViewModel
class FragmentHomeVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private val newJobsState = JobGroupState().apply {
        groupName=application.getString(R.string.new_jobs)
        iconResourceId = R.drawable.ic_new_jobs
    }

    private val inProgressState = JobGroupState().apply {
        groupName=application.getString(R.string.in_progress)
        iconResourceId = R.drawable.ic_in_progress
    }

    private val machinesState = JobGroupState().apply {
        groupName=application.getString(R.string.machines)
        iconResourceId = R.drawable.ic_machine
    }

    init {
        observeJobs()
    }

    fun getStates() = listOf(newJobsState,inProgressState,machinesState)

    private fun observeJobs() {

        viewModelScope.launch(Dispatchers.IO) {

            //Observe NewJobs
            launch {
                try {
                    repository.observeDestination(DatabaseContract.DOCUMENT_DEST_NEW_JOBS)
                        .collect { destination ->
                            renderDestinationToState(destination, newJobsState)
                        }
                } catch (e: Exception) {
                    renderDestinationToState(destination = null, newJobsState)
                }
            }

            //Observe InProgress Jobs
            launch {
                try {
                    repository.observeDestination(DatabaseContract.DOCUMENT_DEST_IN_PROGRESS)
                        .collect { destination ->
                            renderDestinationToState(destination,inProgressState)
                        }
                } catch (e: Exception) {
                    renderDestinationToState(destination=null,newJobsState)
                }
            }

            //Observe Machines Job
            launch {
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
                            renderDestinationToState(it,machinesState)
                        }

                } catch (e: Exception) {
                    e.printStackTrace()
                    renderDestinationToState(destination = null,machinesState)
                }
            }
        }
    }

    /*private fun observeNewJobs() {
        viewModelScope.launch {
            try {
                repository.observeDestination(DatabaseContract.DOCUMENT_DEST_NEW_JOBS)
                    .collect { destination ->
                        renderDestinationToState(destination,newJobsState)
                    }
            } catch (e: Exception) {
                renderDestinationToState(destination = null,newJobsState)
            }
        }
    }

    private fun observeInProgress() {
        viewModelScope.launch {
            try {
                repository.observeDestination(DatabaseContract.DOCUMENT_DEST_IN_PROGRESS)
                    .collect { destination ->
                        renderDestinationToState(destination,inProgressState)
                    }
            } catch (e: Exception) {
                renderDestinationToState(destination=null,newJobsState)
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
                        renderDestinationToState(it,machinesState)
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                renderDestinationToState(destination = null,machinesState)
            }
        }

    }*/

    private fun renderDestinationToState(destination: Destination?, state: JobGroupState) {

        if (destination == null) {
            state.jobCount = application.getString(R.string.xx_jobs, 0)
            state.jobTime = Duration.fromMinutes(0).asFullString()
        } else {
            state.jobCount = application.getString(R.string.xx_jobs, destination.jobCount)
            state.jobTime = Duration.fromMinutes(destination.runningTime).asFullString()
        }

    }

    private fun emptyDestination(destinationName: String): Destination =
        Destination().apply {
            name = destinationName
            type = Destination.TYPE_FIXED
            timeBased = true
        }

}