package com.sivakasi.papco.jobflow.screens.clients

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.screens.clients.ui.ClientDialogState
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ClientsFragmentVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    val clientsList = MutableLiveData<LoadingStatus>()
    val newClientDialogState = MutableLiveData<ClientDialogState?>()
    val editClientDialogState = MutableLiveData<ClientDialogState?>()
    val selectedClient = MutableLiveData<Client>()
    private val queryFlow = MutableStateFlow("")
    var query = mutableStateOf("")


    init {
        loadFilteredClients()
    }

    private fun loadFilteredClients() {

        clientsList.value = LoadingStatus.Loading("")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.loadAllClients()
                    .combine(queryFlow.debounce(500)) { list, query ->
                        if (query.isBlank())
                            return@combine list

                        list.filter { clientToFilter ->
                            clientToFilter.name.lowercase().contains(query,true)
                        }
                    }.map {
                        it.map { client ->
                            ClientUIModel(client.id, client.annotatedName(query.value))
                        }
                    }.collect {
                        clientsList.postValue(LoadingStatus.Success(it))
                    }
            } catch (e: Exception) {
                if (e !is CancellationException)
                    clientsList.postValue(LoadingStatus.Error(e))
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        //Update the compose UI with new query
        query.value = newQuery
        //Now emit the new query in the flow so that the list will get filtered and updated
        queryFlow.value = newQuery
    }

    fun onClientClicked(client: ClientUIModel) {
        val dialogState = ClientDialogState("Edit Client", "SAVE", "CANCEL")
        dialogState.id = client.id
        dialogState.name = client.name.text
        editClientDialogState.value = dialogState
    }

    //Called from UI when the user clicks a client during selecting a client for Print order
    //In selection mode
    fun onClientSelected(client: ClientUIModel) {
        selectedClient.value = Client(client.id, client.name.text)
    }

    fun onFabClicked() {
        val dialogState = ClientDialogState("Add Client", "SAVE", "CANCEL")
        newClientDialogState.value = dialogState
    }

    fun onAddClient(name: String) {

        viewModelScope.launch {
            newClientDialogState.value?.let {
                it.isLoading = true
                try {
                    repository.createClient(Client(name = name))
                    newClientDialogState.value = null
                } catch (e: Exception) {
                    it.isLoading = false
                    Toast.makeText(
                        application,
                        e.message ?: "Unknown Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                newClientDialogState.value = null
            }
        }
    }

    fun cancelNewClientDialog() {
        newClientDialogState.value = null
    }

    fun onUpdateClient(name: String) {
        viewModelScope.launch {
            editClientDialogState.value?.let {
                it.isLoading = true
                try {
                    repository.updateClient(Client(it.id, name))
                    editClientDialogState.value = null
                } catch (e: Exception) {
                    it.isLoading = false
                    Toast.makeText(
                        application,
                        e.message ?: "Unknown Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                editClientDialogState.value = null
            }
        }
    }

    fun cancelEditClientDialog() {
        editClientDialogState.value = null
    }

}