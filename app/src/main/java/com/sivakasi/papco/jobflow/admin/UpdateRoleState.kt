package com.sivakasi.papco.jobflow.admin

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail

class UpdateRoleState(private val context: Context) {

    var email: String by mutableStateOf("")
    var isLoading: Boolean by mutableStateOf(false)
    val roles = listOf("Root", "Admin", "Printer", "Guest")
    var selectedRoleIndex: Int by mutableStateOf(0)
    var error: String? by mutableStateOf(null)
    var emailError: String? by mutableStateOf(null)

    fun isEmailValid(): Boolean {
        return if (email.validEmail()) {
            true
        } else {
            emailError = context.getString(R.string.invalid_email)
            false
        }
    }

    fun startLoading() {
        isLoading = true
    }

    fun loadingFailed(errorMessage: String) {
        isLoading = false
        error = errorMessage
    }

    fun loadingSuccess() {
        isLoading = false
    }
}