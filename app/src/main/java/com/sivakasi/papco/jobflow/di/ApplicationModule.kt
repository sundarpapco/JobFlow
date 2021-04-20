package com.sivakasi.papco.jobflow.di

import android.app.Application
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.sivakasi.papco.jobflow.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideArialFont(application: Application):Typeface?=
        ResourcesCompat.getFont(application, R.font.arial)
}