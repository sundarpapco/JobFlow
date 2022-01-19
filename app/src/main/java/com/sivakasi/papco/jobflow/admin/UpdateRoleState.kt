package com.sivakasi.papco.jobflow.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.data.User

class UpdateRoleState() {

    var selectedUser: User? by mutableStateOf(null)
    var isLoading: Boolean by mutableStateOf(false)
    val roles = listOf("Root", "Admin", "Printer", "Guest")
    var selectedRoleIndex: Int by mutableStateOf(0)
    var error: String? by mutableStateOf(null)

    fun startLoading() {
        error=null
        isLoading = true
    }

    fun loadingFailed(errorMessage: String) {
        isLoading = false
        error = errorMessage
    }

    fun loadingSuccess() {
        error=null
        isLoading = false
    }
}