package com.sivakasi.papco.jobflow

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.Event
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainActivityVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val auth: JobFlowAuth,
    private val repository: Repository
) : ViewModel() {

    val isInitializing = mutableStateOf(false)
    private var userMonitoringJob: Job? = null
    private val _navigateUsingAction = MutableLiveData<Event<Int>>()
    val navigateUsingAction: LiveData<Event<Int>> = _navigateUsingAction

    private val authListener = FirebaseAuth.AuthStateListener {
        initializeCurrentUser()
    }

    init {
        observeTestDocument()
        auth.addAuthStateListener(authListener)
    }

    private fun observeTestDocument(){
        viewModelScope.launch {
            repository.observeTestDocument()
                .collect {
                    if(it==null)
                        Log.d("SUNDAR","The test document is null")
                    else
                        Log.d("SUNDAR","Got a valid Test Document")
                }
        }
    }

    fun initializeCurrentUser() {

        userMonitoringJob?.cancel()

        if (auth.currentUser == null) {
            logOutCurrentUser()
            return
        }

        isInitializing.value = true
        userMonitoringJob = viewModelScope.launch {

            try {
                repository.observeUser(auth.currentUser!!.uid)
                    .collect { user ->
                        if (user == null) {
                            logOutCurrentUser()
                        } else {
                            val oldClaim = getClaim()
                            savedStateHandle["claim"] = auth.fetchUserClaim(auth.currentUser, true)
                            isInitializing.value = false
                            navigateBasedOnClaims(oldClaim, getClaim())
                        }
                    }

            } catch (e: FirebaseNetworkException) {
                //If there is no internet connection detected
                isInitializing.value = false
                _navigateUsingAction.value = Event(R.id.action_global_noInternetFragment)
            }

        }

    }

    private fun logOutCurrentUser() {
        savedStateHandle["claim"] = "none"
        _navigateUsingAction.value = Event(R.id.action_global_loginFragment)
    }

    private fun navigateBasedOnClaims(oldClaim: String, newClaim: String) {

        if (oldClaim == newClaim)
            return

        when (newClaim) {
            "none" -> {
                _navigateUsingAction.value = Event(R.id.action_global_loginFragment)
            }

            "guest" -> {
                _navigateUsingAction.value = Event(R.id.action_global_guestFragment)
            }

            "printer" -> {
                if (oldClaim == "guest" || oldClaim == "none")
                    _navigateUsingAction.value = Event(R.id.action_global_manageMachinesFragment)
                else
                    auth.logout()
            }

            "admin" -> {
                if (oldClaim == "guest" || oldClaim == "none")
                    _navigateUsingAction.value = Event(R.id.action_global_fragmentHome)
                else
                    auth.logout()
            }

            "root" -> {
                if (oldClaim == "guest" || oldClaim == "none")
                    _navigateUsingAction.value = Event(R.id.action_global_fragmentHome)
                else
                    auth.logout()
            }

            else -> {
                error("Invalid user claim detected")
            }
        }

    }

    fun getClaim(): String = savedStateHandle.get<String>("claim") ?: "none"

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}