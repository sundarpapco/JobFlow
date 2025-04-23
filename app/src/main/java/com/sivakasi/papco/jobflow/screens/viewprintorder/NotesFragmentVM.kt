package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrderWithDestination
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.screens.notes.NotesScreenState
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
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

    val screenState = NotesScreenState()

    private var loadedPo: PrintOrderWithDestination? = null
    private var isAlreadyObserving = false

    fun observePrintOrderForRemoval(poNumber: Int,initialNotes:String) {
        if (isAlreadyObserving)
            return
        else
            isAlreadyObserving = true

        screenState.loadInitialNotes(initialNotes)
        viewModelScope.launch {
            try {
                repository.observePrintOrder(poNumber)
                    .collect { po ->
                        if (po != null)
                            loadedPo = po
                        else
                            screenState.showPOMovedDialog()
                    }

            } catch (_: Exception) {

            }
        }
    }


    fun saveNotes() {
        viewModelScope.launch {
            val newNotes=screenState.notes.trim()
            loadedPo?.let {
                screenState.startLoading()
                try {
                     repository.updateNotes(
                        it.destination.id,
                        it.printOrder.documentId(),
                        newNotes
                    )
                    screenState.loadingSuccess()
                } catch (e: Exception) {
                    screenState.loadingError(e)
                }
            } ?: run {
                screenState
                    .loadingError(
                        ResourceNotFoundException(application.getString(R.string.po_not_found))
                    )
            }
        }
    }
}