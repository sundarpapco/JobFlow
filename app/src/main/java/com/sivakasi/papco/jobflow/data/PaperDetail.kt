package com.sivakasi.papco.jobflow.data

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.sivakasi.papco.jobflow.asString
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaperDetail(
    var partyPaper:Boolean=true,
    var height:Float=0f,
    var width:Float=0f,
    var gsm:Int=0,
    var name:String="",
    var sheets:Int=0


):Parcelable {

    override fun toString(): String {
        return "${height.asString()} X ${width.asString()} Cm $gsm GSM $name - $sheets Sheets"
    }
}

class PaperDetailDiff:DiffUtil.ItemCallback<PaperDetail>(){

    override fun areItemsTheSame(oldItem: PaperDetail, newItem: PaperDetail): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: PaperDetail, newItem: PaperDetail): Boolean =
        oldItem == newItem

}
