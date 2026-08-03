package com.sivakasi.papco.jobflow.screens.machines

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.extensions.toastStringResource
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class ManageMachinesVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {
    val uiState = MachinesScreenUIState(application)

    init {
        loadAllMachines()
    }

    private fun loadAllMachines() {
        uiState.machines = LoadingStatus.Loading(application.getString(R.string.one_moment_please))
        viewModelScope.launch(Dispatchers.IO) {
            repository.loadAllMachines()
                .catch {
                    val e = it as? Exception ?: Exception(it)
                    uiState.machines = LoadingStatus.Error(e)
                }
                .collect {
                    uiState.machines = LoadingStatus.Success(it)
                }
        }
    }

    fun deleteMachine(machineId: String) {

        uiState.showWaitDialog()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteMachine(machineId)
                uiState.clearDialog()
            } catch (e: Exception) {
                uiState.clearDialog()
                uiState.toastError(e)
            }
        }
    }

    fun addMachine() {

        val state = (uiState.dialog as MachinesScreenUIState.Dialog.AddMachineDialog).state
        state.isProcessing = true
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val machineName = state.text.text.trim()

                if (repository.machineAlreadyExist(machineName)) {
                    state.isProcessing = false
                    application.toastStringResource(R.string.machine_already_exist)
                } else {
                    repository.createMachine(machineName)
                    uiState.clearDialog()
                }

            } catch (e: Exception) {
                state.isProcessing = false
                application.toastError(e)
            }
        }
    }

    fun editMachine() {
        val state = (uiState.dialog as MachinesScreenUIState.Dialog.EditMachineDialog).state
        val destination = state.data!!
        val newMachineName = state.text.text.trim()

        //If the user has not changed the machine name, then simply don't do anything
        //Just dismiss the dialog
        if (newMachineName == destination.name) {
            uiState.clearDialog()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            state.isProcessing = true
            try {
                if (repository.machineAlreadyExist(newMachineName)) {
                    state.isProcessing = false
                    application.toastStringResource(R.string.machine_already_exist)
                } else {
                    repository.updateMachine(destination.id, newMachineName)
                    uiState.clearDialog()
                }
            } catch (e: Exception) {
                state.isProcessing = false
                uiState.toastError(e)
            }
        }
    }

}