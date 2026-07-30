package com.sivakasi.papco.jobflow.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.sivakasi.papco.jobflow.ui.pink
import kotlinx.serialization.Serializable

sealed interface ClientSelectionPurpose{

    @Serializable
    data object None:ClientSelectionPurpose

    @Serializable
    data object POCreationOrEditing:ClientSelectionPurpose

    @Serializable
    data object History:ClientSelectionPurpose
}


@Serializable
data class Client(
    var id: Int = -1,
    var name: String = "Anonymous Client"
){

    companion object {
        const val FIELD_NAME = "name"
    }

    /*
    This function will generate the annotated string containing the given words highlighted
    if its present in the name string. The returned annotated string will be used by the UI
    to display the name in the searched list highlighting the searched text
     */
    fun annotatedName(selectionString: String):AnnotatedString{

        val builder = AnnotatedString.Builder(name)
        val startingPosition = name.indexOf(selectionString,ignoreCase = true)

        if (startingPosition > -1)
            builder.addStyle(
                SpanStyle(pink),
                startingPosition,
                startingPosition + selectionString.length
            )

        return builder.toAnnotatedString()
    }

    fun documentId(): String {
        require(id > -1)
        return "client$id"
    }
}
