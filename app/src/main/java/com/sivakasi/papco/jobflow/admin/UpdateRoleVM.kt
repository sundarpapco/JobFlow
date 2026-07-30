package com.sivakasi.papco.jobflow.admin

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.User
import com.sivakasi.papco.jobflow.extensions.getMessage
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class UpdateRoleVM @Inject constructor(
    private val application: Application,
    private val auth: JobFlowAuth
) : ViewModel() {

    val state = UpdateRoleState()


    fun selectUser(user: User) {
        state.error = null
        state.selectedUser = user
    }

    fun onUpdateRole() {

        if (state.selectedUser == null)
            return

        state.startLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.updateUserClaim(
                    state.selectedUser!!.email,
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

    fun deleteUser() {

        val selectedUser = state.selectedUser
        val currentUser = auth.currentUser

        if (selectedUser == null || currentUser == null)
            return

        state.startLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("SAAT", "Deleting the user: ${selectedUser.email}")

                //You cannot your own Account
                require(currentUser.email != selectedUser.email) {
                    application.getString(R.string.you_cannot_delete_your_own_account)
                }

                //You cannot delete the primary account
                require(selectedUser.email.lowercase() != "papcopvtltd@gmail.com") {
                    application.getString(R.string.cannot_delete_primary_account)
                }

                //delay(3000)
                auth.deleteUser(selectedUser.email)
                state.selectedUser = null
                withContext(Dispatchers.Main) {
                    state.loadingSuccess()
                    Toast.makeText(
                        application,
                        application.getString(R.string.user_deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                state.loadingFailed(e.getMessage(application))
            }
        }
    }
}