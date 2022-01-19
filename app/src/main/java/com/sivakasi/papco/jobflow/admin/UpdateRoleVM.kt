package com.sivakasi.papco.jobflow.admin

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.data.User
import com.sivakasi.papco.jobflow.extensions.getMessage
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class UpdateRoleVM @Inject constructor(
    private val application: Application,
    private val auth: JobFlowAuth,
    private val repository: Repository
) : ViewModel() {

    val state = UpdateRoleState()
    var users:LoadingStatus by mutableStateOf(
        LoadingStatus.Loading(application.getString(R.string.one_moment_please))
    )

    init {
        loadAllUsers()
    }

    fun selectUser(user:User){
        state.error=null
        state.selectedUser=user
    }


    private fun loadAllUsers(){

        //Send the loading state to UI first
        LoadingStatus.Loading(application.getString(R.string.one_moment_please))

        //Launch the loading process
        viewModelScope.launch(Dispatchers.IO) {
            try{
                repository.getAllUsers()
                    .collect {
                        users = LoadingStatus.Success(it)
                    }
            }catch(e:Exception){
                users = LoadingStatus.Error(e)
            }
        }
    }

    fun onUpdateRole() {

        if(state.selectedUser==null)
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
}