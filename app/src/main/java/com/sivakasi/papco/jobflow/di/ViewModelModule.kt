package com.sivakasi.papco.jobflow.di

import com.sivakasi.papco.jobflow.data.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module
@InstallIn(ViewModelComponent::class)
internal object ViewModelModule {

    @Provides
    fun provideRepository():Repository= Repository()

}