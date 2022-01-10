package com.sivakasi.papco.jobflow

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel:MainActivityVM by viewModels()

    @Inject
    lateinit var auth:JobFlowAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        observeViewModel()
    }

    fun getUserClaim():String = viewModel.getClaim()

    private fun observeViewModel(){

        viewModel.navigateUsingAction.observe(this,EventObserver{action->

            val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
            val navOptions=NavOptions.Builder().setPopUpTo(R.id.nav_graph,false).build()
            navController.navigate(action,null,navOptions)

        })
    }

}