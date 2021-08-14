package com.sivakasi.papco.jobflow.screens.machines

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ManageMachinesVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {


    private val _machines = MutableLiveData<LoadingStatus>()
    private val _deleteStatus = MutableLiveData<Event<LoadingStatus>>()
    val machines: LiveData<LoadingStatus> = _machines
    val deleteStatus: LiveData<Event<LoadingStatus>> = _deleteStatus

    init {
        loadAllMachines()
    }

    private fun loadAllMachines() {
        _machines.value = LoadingStatus.Loading(application.getString(R.string.one_moment_please))
        viewModelScope.launch {
            try {
                repository.loadAllMachines()
                    .collect {
                        _machines.value = LoadingStatus.Success(it)
                    }
            } catch (e: Exception) {
                _machines.value = LoadingStatus.Error(e)
            }
        }
    }

    fun deleteMachine(machineId: String) {
        _deleteStatus.value = loadingEvent(application.getString(R.string.one_moment_please))
        viewModelScope.launch {
            try {
                repository.deleteMachine(machineId)
                _deleteStatus.value = dataEvent(true)
            } catch (e: Exception) {
                _deleteStatus.value = errorEvent(e)
            }
        }
    }

}