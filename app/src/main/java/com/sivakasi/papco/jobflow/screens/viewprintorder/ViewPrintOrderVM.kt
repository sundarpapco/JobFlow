package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ViewPrintOrderVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private var isAlreadyLoaded = false
    private val _loadedPrintOrder = MutableLiveData<LoadingStatus>()
    val loadedPrintOrder: LiveData<LoadingStatus> = _loadedPrintOrder

    fun loadPrintOrder(destinationId: String, printOrderId: String) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch {
            try {
                _loadedPrintOrder.value =
                    LoadingStatus.Loading(application.getString(R.string.one_moment_please))
                repository.observePrintOrder(destinationId, printOrderId)
                    .collect { printOrder ->
                        if (printOrder == null)
                            _loadedPrintOrder.value =
                                LoadingStatus.Error(ResourceNotFoundException(""))
                        else
                            _loadedPrintOrder.value = LoadingStatus.Success(printOrder)
                    }
            } catch (e: Exception) {
                _loadedPrintOrder.value = LoadingStatus.Error(e)
            }
        }

    }

}