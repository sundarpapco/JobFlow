package com.sivakasi.papco.jobflow.screens.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.sivakasi.papco.jobflow.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginFragmentVM @Inject constructor(
    private val application: Application
) : ViewModel() {

    val loginSuccess = MutableLiveData(false)
    val authState = AuthenticationState(application)
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    fun onFormSubmit() {

        authState.email = authState.email.trim()
        authState.password = authState.password.trim()
        authState.confirmPassword = authState.confirmPassword.trim()

        if (!authState.isValid())
            return

        if (authState.mode == AuthenticationMode.LOGIN)
            onLogin()
        else
            onRegisterUsingCloudFunction()
    }

    private fun onLogin() {

        authState.startLogin()
        authState.clearErrors()

        launchLoginCoroutine()
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

    private fun onRegisterUsingCloudFunction() {

        authState.clearErrors()
        authState.startLogin()

        val data = hashMapOf(
            "email" to authState.email,
            "password" to authState.password,
            "displayName" to authState.name
        )

        functions.getHttpsCallable("createNewUser")
            .call(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    launchLoginCoroutine()
                } else {
                    val error = it.exception?.message
                        ?: application.getString(R.string.error_unknown_error)
                    authState.loginFailed(error)
                }
            }
    }

    private fun launchLoginCoroutine() {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(
                authState.email,
                authState.password
            ).addOnSuccessListener {
                authState.loginSuccess()
                loginSuccess.value = true
            }.addOnFailureListener {
                authState.loginFailed(
                    it.message ?: application.getString(R.string.error_unknown_error)
                )
            }
        }
    }

}