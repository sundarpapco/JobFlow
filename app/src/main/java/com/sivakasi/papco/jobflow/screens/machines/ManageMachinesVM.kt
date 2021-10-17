package com.sivakasi.papco.jobflow.screens.machines

import android.app.Application
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.extensions.toastStringResource
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@HiltViewModel
class ManageMachinesVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    val uiState=MachinesScreenUIState(application)

    init {
        loadAllMachines()
    }

    private fun loadAllMachines() {
        uiState.machines = LoadingStatus.Loading(application.getString(R.string.one_moment_please))
        viewModelScope.launch {
            try {
                repository.loadAllMachines()
                    .collect {
                        uiState.machines = LoadingStatus.Success(it)
                    }
            } catch (e: Exception) {
                uiState.machines = LoadingStatus.Error(e)
            }
        }
    }

    fun deleteMachine(machineId: String) {

        uiState.showWaitDialog()
        viewModelScope.launch {
            try {
                repository.deleteMachine(machineId)
                uiState.hideWaitDialog()
            } catch (e: Exception) {
                uiState.hideWaitDialog()
                application.toastError(e)
            }
        }
    }

    fun addMachine(){

        val state=uiState.addMachineDialogState!!
       state.isProcessing=true
        viewModelScope.launch {
            try {

                val machineName = state.text.text.trim()

                if (repository.machineAlreadyExist(machineName)) {
                    state.isProcessing=false
                   application.toastStringResource(R.string.machine_already_exist)
                } else {
                    repository.createMachine(machineName)
                    uiState.hideAddMachineDialog()
                }

            } catch (e: Exception) {
                state.isProcessing=false
                application.toastError(e)
            }
        }
    }

    fun editMachine(){
        val state=uiState.editMachineDialogState!!
        val destination = state.data!!
        val newMachineName = state.text.text.trim()

        //If the user has not changed the machine name, then simply don't do anything
        //Just dismiss the dialog
        if(newMachineName==destination.name){
            uiState.hideEditMachineDialog()
            return
        }

        viewModelScope.launch {
            state.isProcessing=true
            try {
                if (repository.machineAlreadyExist(newMachineName)) {
                    state.isProcessing=false
                    application.toastStringResource(R.string.machine_already_exist)
                } else {
                    repository.updateMachine(destination.id,newMachineName)
                    uiState.hideEditMachineDialog()
                }
            } catch (e: Exception) {
                state.isProcessing=false
                application.toastError(e)
            }
        }
    }

}