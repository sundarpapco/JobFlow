package com.sivakasi.papco.jobflow.nav3.graph

import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.data.ClientSelectionPurpose
import kotlinx.serialization.Serializable
import java.io.Serial

sealed interface AppGraph : NavKey {

    @Serializable
    data object Splash : NavKey

    @Serializable
    data object Guest : NavKey {

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
    data class Machines(val selectionMode: Boolean) : NavKey {
        companion object {
            const val SELECTION_KEY = "jobFlow:SelectedMachines"
        }
    }

    @Serializable
    data class Destination(val destinationId: String, val destinationType: Int) : NavKey

    @Serializable
    data class ViewPrintOrder(val poNumber: Int) : NavKey

    @Serializable
    data class Notes(val poNumber: Int, val initialNotes: String) : NavKey

    @Serializable
    data class PreviousHistory(val plateNumber: Int) : NavKey

    @Serializable
    data class Preview(val imageUrl: String, val previewId: String, val fileName: String) : NavKey

    @Serializable
    data class ManagePreviews(val previewId: String, val title: String) : NavKey

    @Serializable
    data object AlgoliaSearch : NavKey

    //Setting None as Selection Purpose means this is Manage Clients screen and not a selection screen
    @Serializable
    data class Client(
        val selectionPurpose: ClientSelectionPurpose = ClientSelectionPurpose.None
    ) : NavKey {
        companion object {
            const val SELECTION_KEY = "jobFlow:SelectedClient"
        }
    }

    @Serializable
    data class ClientHistory(val client: com.sivakasi.papco.jobflow.data.Client) : NavKey

    @Serializable
    data object InvoiceHistory : NavKey

    @Serializable
    data object UpdateRole : NavKey

    @Serializable
    data object SelectUser: NavKey{

       const val SELECTION_KEY = "jobFlow:SelectedUser"

    }

}