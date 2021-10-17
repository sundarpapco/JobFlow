package com.sivakasi.papco.jobflow.screens.login

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordVM @Inject constructor(
    val application: Application
) : ViewModel() {

    val passwordResetMailSent = MutableLiveData<Boolean>()
    val forgotPasswordState = ForgotPasswordState(application)
    private val auth = FirebaseAuth.getInstance()

    fun onFormSubmit() {

        forgotPasswordState.clearErrors()
        if (!forgotPasswordState.isValid())
            return

        forgotPasswordState.isLoading = true
        viewModelScope.launch {

            auth.sendPasswordResetEmail(forgotPasswordState.email)
                .addOnSuccessListener {
                    Toast.makeText(
                        application,
                        application.getString(R.string.password_reset_mail_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                    forgotPasswordState.isLoading = false
                    passwordResetMailSent.value = true
                }
                .addOnFailureListener {
                    forgotPasswordState.isLoading = false
                    forgotPasswordState.authError =
                        it.message ?: application.getString(R.string.error_unknown_error)
                }

        }

    }
}