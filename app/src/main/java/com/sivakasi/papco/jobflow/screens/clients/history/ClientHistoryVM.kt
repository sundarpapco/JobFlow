package com.sivakasi.papco.jobflow.screens.clients.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.util.Event
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.dataEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ClientHistoryVM @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var alreadyLoaded: Boolean = false
    private val _clientHistory= MutableLiveData<LoadingStatus>()
    private val _clickedResult = MutableLiveData<Event<SearchModel>>()
    val clientHistory:LiveData<LoadingStatus> = _clientHistory
    val clickedResult:LiveData<Event<SearchModel>> = _clickedResult

    init {
        _clientHistory.value = LoadingStatus.Loading("")
    }

    fun loadClientHistory(clientId: Int) {

        if (alreadyLoaded)
            return
        else
            alreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val history=repository.searchByClientId(clientId)
                _clientHistory.postValue(LoadingStatus.Success(history))
            }catch (e:Exception){
                _clientHistory.postValue(LoadingStatus.Error(e))
            }
        }
    }

    fun onResultClicked(searchModel: SearchModel) {
        _clickedResult.value= Event(searchModel)
    }

}