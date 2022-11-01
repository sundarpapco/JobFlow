package com.sivakasi.papco.jobflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JobFlowApplication: Application() {

    companion object{
        const val CHANNEL_ID_PREVIEW_UPLOAD="jobFlow:upload:preview:channel"
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        //Create the Notification channel so that we can display notifications in the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID_PREVIEW_UPLOAD,
            applicationContext.getString(R.string.channel_name_upload_preview),
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}