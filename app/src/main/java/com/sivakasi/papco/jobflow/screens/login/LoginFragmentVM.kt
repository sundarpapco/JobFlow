package com.sivakasi.papco.jobflow.screens.login

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginFragmentVM @Inject constructor(
    private val application: Application
): ViewModel() {

    val loginState=LoginState()
    private val auth = FirebaseAuth.getInstance()

    fun onLogin(){

        loginState.email=loginState.email.trim()
        loginState.password=loginState.password.trim()

        if(!validEmail() || !validPassword())
            return

        loginState.loggingStatus=LoadingStatus.Loading("")
        viewModelScope.launch {
            delay(3000)
            auth.signInWithEmailAndPassword(loginState.email,loginState.password).addOnSuccessListener {
                loginState.loggingStatus=LoadingStatus.Success("")
            }.addOnFailureListener{
                it.printStackTrace()
                loginState.loggingStatus=LoadingStatus.Error(it)
            }
        }
    }

    private fun validEmail():Boolean{

        return if(loginState.email.validEmail()){
            loginState.emailError=null
            true
        }else{
            loginState.emailError=application.getString(R.string.invalid_email)
            false
        }
    }

    private fun validPassword():Boolean{
        return if(loginState.password.length < 8){
            loginState.passwordError=application.getString(R.string.invalid_password)
            false
        } else {
            loginState.passwordError=null
            true
        }
    }

}