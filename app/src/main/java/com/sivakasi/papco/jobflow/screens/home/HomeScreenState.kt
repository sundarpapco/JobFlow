package com.sivakasi.papco.jobflow.screens.home

import android.content.Context
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.util.Duration

class HomeScreenState(private val context: Context){

    val newJobsState = JobGroupState().apply {
        groupName=context.getString(R.string.new_jobs)
        iconResourceId = R.drawable.ic_new_jobs
    }

    val inProgressState = JobGroupState().apply {
        groupName=context.getString(R.string.in_progress)
        iconResourceId = R.drawable.ic_in_progress
    }

    val machinesState = JobGroupState().apply {
        groupName=context.getString(R.string.machines)
        iconResourceId = R.drawable.ic_machine
    }

    fun loadNewJobsDestination(destination: Destination){
        newJobsState.jobCount = context.getString(R.string.xx_jobs, destination.jobCount)
        newJobsState.jobTime = Duration.fromMinutes(destination.runningTime).asFullString()
    }

    fun loadInProgressDestination(destination: Destination){
        inProgressState.jobCount = context.getString(R.string.xx_jobs, destination.jobCount)
        inProgressState.jobTime = Duration.fromMinutes(destination.runningTime).asFullString()
    }

    fun loadMachinesDestination(destination: Destination){
        machinesState.jobCount = context.getString(R.string.xx_jobs, destination.jobCount)
        machinesState.jobTime = Duration.fromMinutes(destination.runningTime).asFullString()
    }

}