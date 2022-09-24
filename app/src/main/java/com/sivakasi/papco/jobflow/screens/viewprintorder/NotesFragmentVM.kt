package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrderWithDestination
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class NotesFragmentVM @Inject constructor(
    private val repository: Repository,
    private val application: Application
) : ViewModel() {

    private val _saveStatus = MutableLiveData<Event<LoadingStatus>>()
    private val _isPrintOrderMovedOrRemoved = MutableLiveData<Boolean>()
    private var loadedPo: PrintOrderWithDestination? = null
    val saveStatus: LiveData<Event<LoadingStatus>> = _saveStatus
    val isPrintOrderMovedOrRemoved: LiveData<Boolean> = _isPrintOrderMovedOrRemoved
    private var isAlreadyObserving = false

    fun observePrintOrderForRemoval(poNumber: Int) {

        if (isAlreadyObserving)
            return
        else
            isAlreadyObserving = true

        viewModelScope.launch {

            try {
                repository.observePrintOrder(poNumber)
                    .collect { po ->
                        if (po != null)
                            loadedPo = po
                        else
                            _isPrintOrderMovedOrRemoved.value = true
                    }

            } catch (_: Exception) {

            }
        }
    }


    fun saveNotes(newNotes: String) {
        viewModelScope.launch {
            loadedPo?.let {
                _saveStatus.value = loadingEvent(application.getString(R.string.one_moment_please))
                try {
                    repository.updateNotes(
                        it.destination.id,
                        it.printOrder.documentId(),
                        newNotes
                    )
                    _saveStatus.value = dataEvent(true)
                } catch (e: Exception) {
                    _saveStatus.value = errorEvent(e)
                }
            } ?: run {
                _saveStatus.value = errorEvent(
                    ResourceNotFoundException(
                        application.getString(R.string.po_not_found)
                    )
                )
            }
        }
    }
}