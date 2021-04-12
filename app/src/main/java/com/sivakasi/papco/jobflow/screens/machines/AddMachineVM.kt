package com.sivakasi.papco.jobflow.screens.machines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.Event
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.dataEvent
import com.sivakasi.papco.jobflow.util.errorEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AddMachineVM @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _saveStatus = MutableLiveData<Event<LoadingStatus>>()
    private val _isLoading = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Event<LoadingStatus>> = _saveStatus
    val isLoading: LiveData<Boolean> = _isLoading

    fun createMachine(machineName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {

                if (repository.machineAlreadyExist(machineName)) {
                    _saveStatus.value = errorEvent(
                        FirebaseFirestoreException(
                            "Hello",
                            FirebaseFirestoreException.Code.ALREADY_EXISTS
                        )
                    )
                } else {
                    repository.createMachine(machineName)
                    _saveStatus.value = dataEvent(true)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _saveStatus.value = errorEvent(e)
            }
        }
    }

    fun updateMachine(machineId: String, machineName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {

                repository.updateMachine(machineId,machineName)
                _saveStatus.value = dataEvent(true)
                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _saveStatus.value = errorEvent(e)
            }
        }
    }
}