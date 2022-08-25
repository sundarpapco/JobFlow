package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application
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
class NotesFragmentVM @Inject constructor(
    private val repository: Repository,
    private val application: Application
):ViewModel() {

    private val _saveStatus=MutableLiveData<Event<LoadingStatus>>()
    private val _isPrintOrderMovedOrRemoved=MutableLiveData<Boolean>()
    val saveStatus:LiveData<Event<LoadingStatus>> =_saveStatus
    val isPrintOrderMovedOrRemoved:LiveData<Boolean> = _isPrintOrderMovedOrRemoved
    private var isAlreadyObserving=false

    fun observePrintOrderForRemoval(destinationId: String,poId: String){

        if(isAlreadyObserving)
            return
        else
            isAlreadyObserving=true

        viewModelScope.launch {
            repository.observePrintOrder(destinationId,poId)
                .collect { printOrder->
                    if(printOrder==null)
                        _isPrintOrderMovedOrRemoved.value = true
                }
        }
    }


    fun saveNotes(destinationId:String,poId:String,newNotes:String){
        viewModelScope.launch {
            _saveStatus.value = loadingEvent(application.getString(R.string.one_moment_please))
            try {
                repository.updateNotes(destinationId, poId, newNotes)
                _saveStatus.value = dataEvent(true)
            }catch(e:Exception){
                _saveStatus.value = errorEvent(e)
            }
        }
    }
}