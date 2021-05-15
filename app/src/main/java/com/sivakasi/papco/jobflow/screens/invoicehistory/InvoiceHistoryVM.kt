package com.sivakasi.papco.jobflow.screens.invoicehistory

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.QuerySnapshot
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.toSearchModel
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class InvoiceHistoryVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private val _invoiceHistory = MutableLiveData<LoadingStatus>()
    val invoiceHistory: LiveData<LoadingStatus> = _invoiceHistory

    init {
        loadHistory()
    }

    private fun loadHistory() {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _invoiceHistory.postValue(LoadingStatus.Loading(application.getString(R.string.one_moment_please)))
                val snapshot = repository.invoiceHistory()
                val result = convertSnapshot(snapshot)
                _invoiceHistory.postValue(LoadingStatus.Success(result))
            }catch(e:Exception){
                _invoiceHistory.postValue(LoadingStatus.Error(e))
            }
        }

    }

    private fun convertSnapshot(snapShot: QuerySnapshot): List<SearchModel> =
        if (snapShot.isEmpty)
            emptyList()
        else
            snapShot.documents.map {
                it.toSearchModel(application)
            }


}