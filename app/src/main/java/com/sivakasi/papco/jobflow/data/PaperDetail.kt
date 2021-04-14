package com.sivakasi.papco.jobflow.data

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.sivakasi.papco.jobflow.extensions.asString
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
        return if(gsm > 0)
            "${height.asString()} X ${width.asString()} Cm $gsm GSM $name - $sheets Sheets"
        else
            "${height.asString()} X ${width.asString()} Cm - $sheets Sheets"
    }

    fun tilePaperSize(trimHeight:Float,trimWidth:Float):Int{

        val landscapeSteps=(height/trimHeight).toInt()*(width/trimWidth).toInt()
        val portraitSteps=(height/trimWidth).toInt()*(width/trimHeight).toInt()
        return maxOf(landscapeSteps,portraitSteps)

    }

    fun paperName():String=
        "${height.asString()} X ${width.asString()} Cm $gsm GSM $name"

    fun paperSize():String="${(height*10).toInt()} X ${(width*10).toInt()} mm"

}

class PaperDetailDiff:DiffUtil.ItemCallback<PaperDetail>(){

    override fun areItemsTheSame(oldItem: PaperDetail, newItem: PaperDetail): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: PaperDetail, newItem: PaperDetail): Boolean =
        oldItem == newItem

}
