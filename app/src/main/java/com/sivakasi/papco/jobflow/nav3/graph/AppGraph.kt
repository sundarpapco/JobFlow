package com.sivakasi.papco.jobflow.nav3.graph

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppGraph: NavKey{

    @Serializable
    data object Splash: NavKey

    @Serializable
    data object Guest : NavKey{

    }

    @Serializable
    data object Login : NavKey

    @Serializable
    data object NoInternet : NavKey

    @Serializable
    data object ForgotPassword : NavKey

    @Serializable
    data object Home : NavKey

    @Serializable
    data class Machines(val selectionMode: Boolean): NavKey{
        companion object{
            const val SELECTION_KEY="jobFlow:SelectedMachines"
        }
    }

    @Serializable
    data class Destination(val destinationId: String, val destinationType:Int): NavKey

    @Serializable
    data class ViewPrintOrder(val poNumber: Int): NavKey

    @Serializable
    data class Notes(val poNumber: Int,val initialNotes:String): NavKey

    @Serializable
    data class PreviousHistory(val plateNumber:Int): NavKey

    @Serializable
    data class Preview(val imageUrl:String,val previewId:String,val fileName:String): NavKey

    @Serializable
    data class ManagePreviews(val previewId:String,val title:String): NavKey

    @Serializable
    data class Client(val isSelectionMode: Boolean) : NavKey{
        companion object{
            const val SELECTION_KEY="jobFlow:SelectedClient"
        }
    }
}