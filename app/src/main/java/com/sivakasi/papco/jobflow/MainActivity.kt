package com.sivakasi.papco.jobflow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var loggedInUserClaim:String="none"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        savedInstanceState?.let{
            loggedInUserClaim=it.getString("role","none")
        }
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("role",loggedInUserClaim)
    }

    fun saveUserClaim(claim:String){
        loggedInUserClaim=claim
    }

    fun getUserClaim():String = loggedInUserClaim
}