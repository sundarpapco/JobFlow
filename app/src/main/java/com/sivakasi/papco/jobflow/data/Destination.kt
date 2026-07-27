package com.sivakasi.papco.jobflow.data

import com.google.firebase.firestore.Exclude

data class Destination(
    @Exclude
    var id:String="",
    var type:Int= TYPE_DYNAMIC,
    var name:String="Destination",
    var jobCount:Int=0,
    var runningTime: Int=0,
    var lastJobCompletion:Long=0,
    var timeBased:Boolean=false,
    var creationTime:Long=0 //Used to sort the destinations in the machines list screen
){
    companion object{
        const val TYPE_FIXED=1;
        const val TYPE_DYNAMIC=2;
    }
}
