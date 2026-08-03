package com.sivakasi.papco.jobflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.nav3.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JobFlowMaterial3Theme(
                darkTheme = true
            ){
                MainScreen()
            }
        }
    }

}