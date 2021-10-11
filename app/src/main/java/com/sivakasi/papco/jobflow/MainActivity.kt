package com.sivakasi.papco.jobflow

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.sivakasi.papco.jobflow.util.AuthStateChange
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

    fun saveUserClaim(claim:String){
        viewModel.saveClaim(claim)
    }

    fun getUserClaim():String = viewModel.getClaim()

    private fun observeViewModel(){

        viewModel.authChanged.observe(this){change->

            if(change==AuthStateChange.LoggedOut)
                logOutUser()

            if(change == AuthStateChange.DeActivated)
                deActivateUser()

            if(change == AuthStateChange.Activated)
                activateUser()

            if(change == AuthStateChange.RoleChanged)
                restartApp()

        }
    }

    fun logOutUser(){
        auth.logout()
        saveUserClaim("none")
        val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
        val navOptions=NavOptions.Builder().setPopUpTo(R.id.nav_graph,false).build()
        navController.navigate(R.id.action_global_loginFragment,null,navOptions)
    }

    private fun deActivateUser(){
        val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
        val navOptions=NavOptions.Builder().setPopUpTo(R.id.nav_graph,false).build()
        navController.navigate(R.id.action_global_guestFragment,null,navOptions)
    }

    private fun activateUser(){
        val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
        val navOptions=NavOptions.Builder().setPopUpTo(R.id.nav_graph,false).build()
        navController.navigate(R.id.action_global_loginFragment,null,navOptions)
    }

    private fun restartApp(){
        val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
        val navOptions=NavOptions.Builder().setPopUpTo(R.id.nav_graph,false).build()
        navController.navigate(R.id.action_global_loginFragment,null,navOptions)
    }

}