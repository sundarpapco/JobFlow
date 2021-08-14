package com.sivakasi.papco.jobflow.data

import android.os.Parcelable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.google.firebase.firestore.Exclude
import com.sivakasi.papco.jobflow.ui.pink
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    var id: Int = -1,
    var name: String = "Anonymous Client"
) : Parcelable {

    companion object {
        const val FIELD_ID = "id"
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
