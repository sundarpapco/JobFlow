package com.sivakasi.papco.jobflow.util

import android.view.View

interface RecyclerAdapterCallBack<T>{
    fun onListItemClicked(view: View, item:T, position:Int)
    fun onListItemLongClicked(item:T,position:Int){
        //Just a blank implementation
    }

    /*Should be used when using shared element transition as this recycler fragment as source

    Recycler adapter should call this immediately after binding an item with the shared transition
    view so that the listening fragment can resume the postponed shared transition if there is any
     */
    fun onBindSharedTransitionElement(){
        //Just a blank default implementation
    }
}