package com.sivakasi.papco.jobflow

import androidx.lifecycle.*
import com.google.firebase.FirebaseNetworkException
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.util.AuthStateChange
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

    private var userMonitoringJob: Job? = null
    private val _authChanged = MutableLiveData<AuthStateChange>()
    val authChanged: LiveData<AuthStateChange> = _authChanged


    init {
        saveClaim(getClaim())
    }

    fun saveClaim(claim: String) {

        savedStateHandle["claim"] = claim
        userMonitoringJob?.cancel()

        if (claim != "none")
            auth.currentUser?.let {
                userMonitoringJob = startMonitoringTheUser(it.uid)
            }
    }

    fun getClaim(): String = savedStateHandle.get<String>("claim") ?: "none"


    private fun startMonitoringTheUser(userId: String) =
        viewModelScope.launch {
            try {
                repository.observeUser(userId)
                    .collect { user ->
                        if (user == null) {
                            //User deleted. So, logout of the app immediately
                            _authChanged.postValue(AuthStateChange.LoggedOut)
                        } else {
                            //Something happened to the claim. Refresh, Check and Act accordingly
                            val oldClaim = getClaim()
                            savedStateHandle["claim"] = auth.fetchUserClaim(auth.currentUser, true)
                            _authChanged.value = auth.checkForAuthChange(oldClaim, getClaim())
                            //}
                        }
                    }
            } catch (e: FirebaseNetworkException) {

                //Handle the case when the user cannot be monitored for the change due to
                //No internet connectivity
                //Not sure how to handle this. For now, we are simply omitting this exception
                //And so the user will not be monitored for this session
            }
        }
}