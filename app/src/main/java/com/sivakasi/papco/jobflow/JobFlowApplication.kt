package com.sivakasi.papco.jobflow

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class JobFlowApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}