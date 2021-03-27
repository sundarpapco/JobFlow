package com.sivakasi.papco.jobflow.util

sealed class LoadingStatus{

    class Loading(val msg:String):LoadingStatus()
    class Success<Y>(val data:Y):LoadingStatus()
    class Error(val exception:Exception):LoadingStatus()

}

class LoadingFailedException(message:String):Exception(message)