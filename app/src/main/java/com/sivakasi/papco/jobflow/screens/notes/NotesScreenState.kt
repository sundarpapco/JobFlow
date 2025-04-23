package com.sivakasi.papco.jobflow.screens.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed class NotesScreenDialogs{
    data object None:NotesScreenDialogs()
    data object WaitDialog : NotesScreenDialogs()
    data object ExitConfirmation:NotesScreenDialogs()
    data object PONotFound:NotesScreenDialogs()
}

class NotesScreenState {

    private val _saveStatus = Channel<LoadingStatus>()
    val saveStatus = _saveStatus.receiveAsFlow()

    var dialog:NotesScreenDialogs by mutableStateOf(NotesScreenDialogs.None)
    private set

    var initialNotes:String=""
        private set

    var notes by mutableStateOf("")

    fun loadInitialNotes(initialNotes:String){
        notes=initialNotes
        this.initialNotes=initialNotes
    }

    fun startLoading(){
        dialog=NotesScreenDialogs.WaitDialog
    }

    fun stopLoading(){
        dialog=NotesScreenDialogs.None
    }

    suspend fun loadingSuccess(){
        dialog=NotesScreenDialogs.None //Hide Wait dialog
        _saveStatus.send(LoadingStatus.Success(true))
    }

    suspend fun loadingError(e:Exception){
        dialog=NotesScreenDialogs.None //Hide Wait Dialog
        _saveStatus.send(LoadingStatus.Error(e))
    }

    fun showExitConfirmationDialog(){
        dialog=NotesScreenDialogs.ExitConfirmation
    }

    fun showPOMovedDialog(){
        dialog=NotesScreenDialogs.PONotFound
    }

    fun hideDialog(){
        dialog=NotesScreenDialogs.None
    }

}