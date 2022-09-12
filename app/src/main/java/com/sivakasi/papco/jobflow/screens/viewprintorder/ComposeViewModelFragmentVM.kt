package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.app.Application
import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.shareReport
import com.sivakasi.papco.jobflow.print.PrintOrderReport
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@HiltViewModel
class ComposeViewModelFragmentVM @Inject constructor(
    private val repository: Repository,
    private val application: Application,
    val printOrderReport: PrintOrderReport
) : ViewModel() {


    private var isAlreadyLoaded = false
    private var loadedPrintOrder: PrintOrder? = null
    val screenState = ViewPrintOrderScreenState()


    fun loadPrintOrder(destinationId: String, printOrderId: String, userRole: String) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {

            launch {
                try {
                    repository.observePrintOrder(destinationId, printOrderId)
                        .collect { printOrder ->
                            if (printOrder == null) {
                                loadedPrintOrder = null
                                screenState.printOrderMoved()
                            } else {
                                loadedPrintOrder = printOrder
                                screenState.loadPrintOrder(
                                    application, printOrder, destinationId, userRole
                                )
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    screenState.loadError(application, e)
                }

            }

            launch {
                try {
                    repository.observeDestination(destinationId)
                        .collect { destination ->
                            destination?.let {
                                screenState.destinationName = it.name
                            } ?: screenState.printOrderMoved()
                        }
                } catch (e: Exception) {
                    screenState.loadError(application, e)
                }
            }

        }

    }


}