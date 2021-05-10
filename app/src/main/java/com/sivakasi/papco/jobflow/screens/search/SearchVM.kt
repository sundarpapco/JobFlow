package com.sivakasi.papco.jobflow.screens.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SearchVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private var searchJob: Job? = null
    private val _searchStatus = MutableLiveData<LoadingStatus>()
    val searchStatus: LiveData<LoadingStatus> = _searchStatus

    fun search(searchQuery: String) {

        //Cancel the previous search job if any and launch new search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _searchStatus.value =
                    LoadingStatus.Loading(application.getString(R.string.one_moment_please))
                val resultList = repository.search(searchQuery)
                _searchStatus.value = LoadingStatus.Success(resultList)
            } catch (e: Exception) {
                _searchStatus.value = LoadingStatus.Error(e)
            } finally {
                searchJob = null
            }
        }
    }
}