package com.sivakasi.papco.jobflow.screens.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.extensions.getMessage
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginFragmentVM @Inject constructor(
    private val application: Application,
    private val auth: JobFlowAuth
) : ViewModel() {

    val loginSuccess = MutableLiveData("none")
    val authState = AuthenticationState(application)

    init {
        checkUserStatus()
    }

    fun checkUserStatus() {


        //Hide the splash screen and show the login screen if no user is logged in
        if (auth.currentUser == null) {
            authState.isSplashScreenShown = false
            return
        }

        //This method will be called when the user press the TRY AGAIN button in the
        //No Internet connection fragment. So, we should show the progress bar in that screen
        //If that screen is showing now
        authState.internetConnectionState.isReconnecting = true

        //Some user has logged in. so, check the claim and inform the fragment to navigate
        viewModelScope.launch {

            try {
                val claim = auth.fetchUserClaim(auth.currentUser)
                loginSuccess.value = claim
            } catch (e: Exception) {
                //Update the UI to the no internet screen
                authState.internetConnectionState.isInternetConnected = false
            }

            authState.internetConnectionState.isReconnecting = false
        }
    }

    fun onFormSubmit() {

        authState.email = authState.email.trim()
        authState.password = authState.password.trim()
        authState.confirmPassword = authState.confirmPassword.trim()

        if (!authState.isValid())
            return

        authState.startLogin()
        authState.clearErrors()

        if (authState.mode == AuthenticationMode.LOGIN)
            logInAndGetClaim(registerBeforeLogin = false)
        else
            logInAndGetClaim(registerBeforeLogin = true)
    }


    fun onModeChanged(targetMode: AuthenticationMode) {
        authState.authError = null
        if (targetMode == AuthenticationMode.LOGIN) {
            authState.confirmPassword = ""
            authState.confirmPasswordError = null
            authState.name = ""
            authState.nameError = null
        }
        authState.mode = targetMode
    }


    private fun logInAndGetClaim(registerBeforeLogin: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                if (registerBeforeLogin)
                    auth.registerUser(authState.email, authState.password, authState.name)

                val loggedInUser = auth.logIn(authState.email, authState.password)
                val loggedInUserClaim = auth.fetchUserClaim(loggedInUser.user)
                loginSuccess.postValue(loggedInUserClaim)

            } catch (e: Exception) {
                authState.loginFailed(e.getMessage(application))
            }
        }
    }

}