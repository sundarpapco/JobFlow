package com.sivakasi.papco.jobflow.data

import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.firestore.Exclude

data class Destination(
    @Exclude
    var id:String="",
    var type:Int= TYPE_DYNAMIC,
    var name:String="Destination",
    var jobCount:Int=0,
    var runningTime: Int=0,
    var lastJobCompletion:Long=0,
    var timeBased:Boolean=false
){
    companion object{
        const val TYPE_FIXED=1;
        const val TYPE_DYNAMIC=2;
    }
}

class DestinationDiff:DiffUtil.ItemCallback<Destination>(){
    override fun areItemsTheSame(oldItem: Destination, newItem: Destination): Boolean {
        return oldItem==newItem
    }

    override fun areContentsTheSame(oldItem: Destination, newItem: Destination): Boolean {
        return oldItem==newItem
    }
}
