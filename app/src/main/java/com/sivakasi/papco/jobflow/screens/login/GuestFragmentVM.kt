package com.sivakasi.papco.jobflow.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuestFragmentVM @Inject constructor(
    private val auth: JobFlowAuth
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    val accountActivated = MutableLiveData("guest")

    fun onRefresh() {

        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val claim = auth.fetchUserClaim(auth.currentUser, true)
                if (claim != "guest")
                    accountActivated.postValue(claim)
            } catch (e: Exception) {
                isLoading.value=false
            }

        }
    }
}