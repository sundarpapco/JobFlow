package com.sivakasi.papco.jobflow.util

import kotlin.math.roundToInt

class Duration(hours: Int = 0, minutes: Int = 0) {

    companion object {

        fun fromMinutes(minutes: Int): Duration {
            return Duration(
                minutes / 60,
                minutes % 60
            )
        }
    }


    var hours:Int=0
    set(value){
        field = if(value<0) 0 else value
    }

    var minutes:Int=0
    set(value){
        when{
            value < 0 ->{ field=0}
            value >=60 ->{
                hours+=value/60
                field = value%60
            }
            else->{field=value}
        }
    }

    init {
        this.hours=hours
        this.minutes= minutes
    }

    operator fun plus(duration: Duration): Duration {
        return fromMinutes(
            inMinutes() + duration.inMinutes()
        )
    }

    operator fun minus(duration: Duration): Duration {

        val resultInMinutes = inMinutes() - duration.inMinutes()
        return when {
            resultInMinutes < 0 -> {
                Duration(0, 0)
            }

            else -> {
                fromMinutes(
                    resultInMinutes
                )
            }
        }
    }

    fun inMinutes(): Int {
        return hours * 60 + minutes
    }

    fun timeFormatString():String{
        return String.format("%02d:%02d",hours,minutes)
    }

    private fun asString(): String {
        return "$hours Hrs, $minutes Mins"
    }

    override fun toString(): String {
        return asString()
    }


    fun asDecimal():Double{
        return hours.toDouble()+ ((minutes.toDouble()/60.0*100).roundToInt()).toDouble()/100.0
    }


    override fun equals(other: Any?): Boolean {
        if(other==null) return false
        val arg: Duration
        try{
            arg=other as Duration
        }catch (e:Exception){
            return false
        }
        return hours==arg.hours && minutes==arg.minutes
    }

    override fun hashCode(): Int {
        var result = hours
        result = 31 * result + minutes
        return result
    }

}