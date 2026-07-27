package com.sivakasi.papco.jobflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.sivakasi.papco.jobflow.nav3.MainScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel:MainActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JobFlowTheme {
                MainScreen()
            }
        }
    }

    fun getUserClaim():String = viewModel.getClaim()

}