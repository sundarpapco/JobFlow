package com.sivakasi.papco.jobflow.admin

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.getMessage
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UpdateRoleVM @Inject constructor(
    private val application: Application,
    private val auth: JobFlowAuth
) : ViewModel() {

    val state = UpdateRoleState(application)

    fun onUpdateRole() {

        if (!state.isEmailValid())
            return

        state.startLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.updateUserClaim(
                    state.email,
                    state.roles[state.selectedRoleIndex].lowercase(Locale.getDefault())
                )
                //We can toast only from the main thread. So, switching to main thread
                withContext(Dispatchers.Main) {
                    state.loadingSuccess()
                    Toast.makeText(
                        application,
                        application.getString(R.string.role_update_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                state.loadingFailed(e.getMessage(application))
            }
        }

    }
}