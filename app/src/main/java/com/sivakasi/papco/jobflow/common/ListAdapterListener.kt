package com.sivakasi.papco.jobflow.common

interface ListAdapterListener<T> {
    fun onItemClick(item:T,position:Int)
    fun onItemLongClick(item:T,position:Int){}
    fun onItemMoved(updatingJobs:List<T>){}
}