package com.sivakasi.papco.jobflow

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainActivityVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val auth: JobFlowAuth,
    private val repository: Repository
) : ViewModel() {

    var isInitializing by mutableStateOf(false)
    private var userMonitoringJob: Job? = null

    private var _landingHomeScreen = Channel<NavKey>()
    val landingHomeScreen = _landingHomeScreen.receiveAsFlow()
    var userClaim: String? by mutableStateOf(null)

    private val authListener = FirebaseAuth.AuthStateListener {
        initializeCurrentUser()
    }

    init {
        Log.d("SAAT", "Main Activity Creating")
        auth.addAuthStateListener(authListener)
    }

    fun initializeCurrentUser() {

        Log.d("SAAT", "Initializing current user")
        userMonitoringJob?.cancel()

        if (auth.currentUser == null) {
            logOutCurrentUser()
            return
        }

        isInitializing = true
        userMonitoringJob = viewModelScope.launch {

            repository.observeUser(auth.currentUser!!.uid)
                .catch {
                    //If there is no internet connection detected
                    isInitializing = false
                    _landingHomeScreen.send(AppGraph.NoInternet)
                }
                .collect { user ->
                    if (user == null) {
                        logOutCurrentUser()
                    } else {
                        val oldClaim = getClaim()
                        savedStateHandle["claim"] = auth.fetchUserClaim(auth.currentUser, true)
                        isInitializing = false
                        userClaim = getClaim()
                        navigateBasedOnClaims(oldClaim, getClaim())
                    }
                }

        }

    }

    fun logOutCurrentUser() {
        savedStateHandle["claim"] = "none"
        userClaim = "none"
        _landingHomeScreen.trySend(AppGraph.Login)
    }

    private fun navigateBasedOnClaims(oldClaim: String, newClaim: String) {

        if (oldClaim == newClaim)
            return

        val destination = when (newClaim) {
            "none" -> {
                AppGraph.Login
            }

            "guest" -> {
                AppGraph.Guest
            }

            "printer" -> {
                AppGraph.Machines(false)
            }

            "admin" -> {
                AppGraph.Home
            }

            "root" -> {
                AppGraph.Home
            }

            else -> error("Invalid user claim detected")
        }

        _landingHomeScreen.trySend(destination)

    }

    fun getClaim(): String = savedStateHandle.get<String>("claim") ?: "none"

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
    }
}