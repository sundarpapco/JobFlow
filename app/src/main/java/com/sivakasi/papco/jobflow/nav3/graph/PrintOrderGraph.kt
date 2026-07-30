package com.sivakasi.papco.jobflow.nav3.graph

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class PrintOrderGraph(
    val editingPONumber: Int?,
    val parentDestinationId: String,
    val autoRepeat: Boolean
) : NavKey {

    @Serializable
    data class AddPO(val editingPONumber: Int?,val parentDestinationId:String,val autoRepeat: Boolean) : NavKey

    @Serializable
    data object JobDetails : NavKey

    @Serializable
    data object PaperDetails : NavKey

    @Serializable
    data object PlateMakingDetails : NavKey

    @Serializable
    data object PrintingDetails : NavKey

    @Serializable
    data object PostPressDetails : NavKey

}