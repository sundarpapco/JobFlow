package com.sivakasi.papco.jobflow.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class JobGroupState {
    var iconResourceId by mutableStateOf(0)
    var groupName by mutableStateOf("")
    var jobCount by mutableStateOf("Loading...")
    var jobTime by mutableStateOf("Loading...")
}