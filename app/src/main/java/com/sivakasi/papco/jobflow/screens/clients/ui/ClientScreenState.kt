package com.sivakasi.papco.jobflow.screens.clients.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.ui.TextInputDialogState
import kotlinx.coroutines.flow.MutableStateFlow

class ClientScreenState(
    private val context: Context
) {

    var dialogState:TextInputDialogState<Int>? by mutableStateOf(null)
    var query = MutableStateFlow("")

    var clientList:List<ClientUIModel> by mutableStateOf(emptyList())
    private set
    var isLoading by mutableStateOf(true)
    private set
    var loadingError:String? by mutableStateOf(null)
    private set

    fun showAddClientDialog(){
        val addDialogState = TextInputDialogState<Int>("SAVE", "CANCEL").apply{
            title=context.getString(R.string.add_client)
            label=context.getString(R.string.client_name)
        }
        dialogState=addDialogState
    }

    fun showEditClientDialog(id:Int,name:String){
        val editDialogState = TextInputDialogState<Int>("SAVE", "CANCEL").apply {
            title=context.getString(R.string.edit_client)
            label=context.getString(R.string.client_name)
            data=id
            //Put the cursor at the end of the text
            text = TextFieldValue( name, TextRange(name.length,name.length))
        }
        dialogState = editDialogState
    }

    fun loadClientList(list:List<ClientUIModel>){
        isLoading=false
        loadingError=null
        clientList= list
    }

    fun loadingError(e:Exception){

        val message = e.message ?: context.getString(R.string.error_unknown_error)
        isLoading=false
        loadingError=message
    }

    fun hideDialog(){
        dialogState=null
    }

}