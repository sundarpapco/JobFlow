package com.sivakasi.papco.jobflow.screens.clients

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.screens.clients.ui.ClientScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ClientsFragmentVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    val screenState = ClientScreenState(application)


    init {
        loadFilteredClients()
    }

    private fun loadFilteredClients() {

        viewModelScope.launch(Dispatchers.IO) {
                repository.loadAllClients()
                    .combine(screenState.query
                        .debounce(500.milliseconds)
                        .onStart { emit("") }
                    ) { list, query ->
                        if (query.isBlank())
                            return@combine list

                        list.filter { clientToFilter ->
                            clientToFilter.name.lowercase().contains(query, true)
                        }
                    }.map {
                        it.map { client ->
                            ClientUIModel(client.id, client.annotatedName(screenState.query.value))
                        }
                    }.catch {
                        val e = it as? Exception ?: Exception(it)
                        screenState.loadingError(e)

                    }.collect {
                        screenState.loadClientList(it)
                    }
        }
    }


    fun onAddClient(name: String) {

        viewModelScope.launch {
            screenState.dialogState?.let {
                it.isProcessing = true
                try {
                    repository.createClient(Client(name = name))
                    screenState.hideDialog()
                } catch (e: Exception) {
                    it.isProcessing = false
                    Toast.makeText(
                        application,
                        e.message ?: "Unknown Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }



    fun onUpdateClient(name: String) {
        viewModelScope.launch {
           screenState.dialogState?.let {
                it.isProcessing = true
                require(it.data!=null){"Client ID should not be null while updating a client"}
                try {
                    repository.updateClient(Client(it.data!!, name))
                    screenState.hideDialog()
                } catch (e: Exception) {
                    it.isProcessing = false
                    Toast.makeText(
                        application,
                        e.message ?: "Unknown Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

}