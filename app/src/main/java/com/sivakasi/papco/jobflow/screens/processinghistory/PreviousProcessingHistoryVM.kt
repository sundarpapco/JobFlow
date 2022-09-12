package com.sivakasi.papco.jobflow.screens.processinghistory

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.data.completeProcessingHistory
import com.sivakasi.papco.jobflow.data.toSearchModel
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PreviousProcessingHistoryVM
@Inject constructor(
    val repository: Repository,
    val application: Application
) : ViewModel() {

    private var alreadyLoaded: Boolean = false
    var screenToRender: LoadingStatus? by mutableStateOf(null)

    fun loadPreviousHistoryOfPlateNumber(plateNumber: Int) {

        if (alreadyLoaded)
            return
        else
            alreadyLoaded = true

        screenToRender = LoadingStatus.Loading(application.getString(R.string.one_moment_please))
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val printOrder = repository.getLastCompletedPrintOrderWithPlateNumber(plateNumber)
                printOrder?.let {
                    screenToRender = LoadingStatus.Success(
                        PreviousHistoryScreenState(
                            //Since we are searching only from the Completed destination, we can straight away pass that
                            //Destination name here
                            it.toSearchModel(application, DatabaseContract.DOCUMENT_DEST_COMPLETED),
                            it.completeProcessingHistory(application)
                        )
                    )
                } ?: run {
                    screenToRender = LoadingStatus.Error(
                        ResourceNotFoundException(
                            application.getString(R.string.cannot_get_due_to_no_internet)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                screenToRender = LoadingStatus.Error(e)
            }


        }
    }

}