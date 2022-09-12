package com.sivakasi.papco.jobflow.screens.login

import android.app.Application
import android.util.Log
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

    val authState = AuthenticationState(application)


    fun onFormSubmit() {

        authState.email = authState.email.trim()
        authState.password = authState.password.trim()
        authState.confirmPassword = authState.confirmPassword.trim()

        if (!authState.isValid())
            return

        authState.startLogin()
        authState.clearErrors()

        if (authState.mode == AuthenticationMode.LOGIN)
            logIn(registerBeforeLogin = false)
        else
            logIn(registerBeforeLogin = true)
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


    private fun logIn(registerBeforeLogin: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                /*Once successfully logged in, MainActivityVM is notified via the AuthStateSListener
                And it will take care of navigating to the next fragment after validating the logged user
                and claim. So, we don't need to do anything here except keep showing the progressBar*/

                if (registerBeforeLogin)
                    auth.registerUser(authState.email, authState.password, authState.name)

                Log.d("SUNDAR","Logging In")
                auth.logIn(authState.email, authState.password)
                Log.d("SUNDAR","Logged In Successfully")

            } catch (e: Exception) {
                authState.loginFailed(e.getMessage(application))
            }
        }
    }

}