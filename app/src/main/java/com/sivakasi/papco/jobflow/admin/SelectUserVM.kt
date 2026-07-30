package com.sivakasi.papco.jobflow.admin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SelectUserVM @Inject constructor(
    val application: Application,
    val repository: Repository
) : ViewModel() {


    var users: LoadingStatus by mutableStateOf(
        LoadingStatus.Loading(application.getString(R.string.one_moment_please))
    )

    init {
        loadAllUsers()
    }

    private fun loadAllUsers() {

        //Send the loading state to UI first
        LoadingStatus.Loading(application.getString(R.string.one_moment_please))

        //Launch the loading process
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllUsers()
                .catch {
                    val e = it as? Exception ?: Exception(it)
                    users = LoadingStatus.Error(e)
                }
                .collect {
                    users = LoadingStatus.Success(it)
                }
        }
    }
}