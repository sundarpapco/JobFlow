package com.sivakasi.papco.jobflow.screens.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail

class ForgotPasswordState(private val context:Context) {

    var email:String by mutableStateOf("")
    var emailError:String? by mutableStateOf(null)
    var isLoading:Boolean by mutableStateOf(false)
    var authError:String? by mutableStateOf(null)

    private fun isEmailValid():Boolean{
        return if(email.validEmail()){
            emailError=null
            true
        }else{
            emailError=context.getString(R.string.invalid_email)
            false
        }
    }

    fun clearErrors(){
        emailError=null
        authError=null
    }

    fun isValid()=isEmailValid()

}