package com.sivakasi.papco.jobflow.screens.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.util.FormValidator
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail

enum class AuthenticationMode {
    LOGIN, SIGNUP
}

class AuthenticationState(private val context: Context) {

    var mode: AuthenticationMode by mutableStateOf(AuthenticationMode.LOGIN)
    var isSplashScreenShown by mutableStateOf(true)
    var authError: String? by mutableStateOf(null)
    var isLoading: Boolean by mutableStateOf(false)
    var name:String by mutableStateOf("")
    var nameError:String? by mutableStateOf(null)
    var email: String by mutableStateOf("")
    var emailError: String? by mutableStateOf(null)
    var password: String by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)
    var passwordError: String? by mutableStateOf(null)
    var confirmPassword: String by mutableStateOf("")
    var confirmPasswordError: String? by mutableStateOf(null)
    var internetConnectionState = InternetConnectionState()

    private fun isEmailValid(): Boolean {
        return if (email.validEmail()) {
            emailError = null
            true
        } else {
            emailError = context.getString(R.string.invalid_email)
            false
        }
    }

    private fun isValidPassword(): Boolean {
        return if (password.isBlank() || password.length < 8) {
            passwordError = context.getString(R.string.invalid_password)
            false
        } else {
            passwordError = null
            true
        }
    }

    private fun isNameValid():Boolean {

        return if(name.isNotBlank()){
            nameError=null
            true
        }else{
            nameError=context.getString(R.string.required_field)
            false
        }
    }

    private fun isPasswordAndConfirmationMatches(): Boolean {
        return if (password == confirmPassword) {
            confirmPasswordError = null
            true
        } else {
            confirmPasswordError = context.getString(R.string.confirmation_password_match_error)
            false
        }
    }

    fun isValid(): Boolean {

        return if(mode==AuthenticationMode.LOGIN) {
            FormValidator()
                .validate(isEmailValid())
                .validate(isValidPassword())
                .isValid()
        }else {
            FormValidator()
                .validate(isNameValid())
                .validate(isEmailValid())
                .validate(isPasswordAndConfirmationMatches())
                .validate(isValidPassword())
                .isValid()
        }

    }

    fun clearErrors() {
        nameError=null
        emailError = null
        passwordError = null
        authError = null
        confirmPasswordError=null
    }

    fun startLogin() {
        isLoading = true
    }

    fun loginSuccess() {
        isLoading = false
    }

    fun loginFailed(error: String) {
        isLoading = false
        authError = error
    }
}

class InternetConnectionState{
    var isInternetConnected by mutableStateOf(true)
    var isReconnecting by mutableStateOf(false)
}