package com.sivakasi.papco.jobflow.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.util.LoadingStatus

class LoginState {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var emailError:String? by mutableStateOf(null)
    var passwordError:String? by mutableStateOf(null)
    var loggingStatus:LoadingStatus? by mutableStateOf(null)
}