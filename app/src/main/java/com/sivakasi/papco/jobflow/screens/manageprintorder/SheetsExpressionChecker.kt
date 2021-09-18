package com.sivakasi.papco.jobflow.screens.manageprintorder

import java.lang.IllegalArgumentException
import java.util.regex.Pattern

class SheetsExpressionChecker(private val userInput: String) {

    private val regEx = "\\d+[rgs](\\d+)?(x\\d+)?(\\+\\d+[rgs](\\d+)?(x\\d+)?)*"

    val isValid: Boolean by lazy {
        Pattern.matches(regEx, userInput)
    }

    private fun parseStringToSets(): List<String> {
        val sets = userInput.split("+")
        return if (sets.isEmpty())
            return emptyList()
        else
            sets
    }

    //Will parse a unit string like 22r350 or 22g or 33s and return the corresponding sheets
    private fun parseUnit(unitString: String): Int {

        val rIndex = unitString.indexOf("r", 0, true)
        val gIndex = unitString.indexOf("g", 0, true)
        val sIndex = unitString.indexOf("s", 0, true)

        val unitQuantity:Int
        val sheetsQuantity:String

        val multiplyingFactor = when {

            rIndex != -1 -> {
                unitQuantity = unitString.substring(0, rIndex).toInt()
                sheetsQuantity = unitString.substring(rIndex+1,unitString.length)
                500
            }

            gIndex != -1 -> {
                unitQuantity = unitString.substring(0, gIndex).toInt()
                sheetsQuantity = unitString.substring(gIndex+1,unitString.length)
                144
            }

            sIndex != -1 -> {
                unitQuantity = unitString.substring(0, sIndex).toInt()
                sheetsQuantity = unitString.substring(sIndex+1,unitString.length)
                1
            }

            else -> {
                unitQuantity = 0
                sheetsQuantity=""
                0
            }
        }

        return if(sheetsQuantity.isNotBlank())
            unitQuantity * multiplyingFactor + sheetsQuantity.toInt()
        else
            unitQuantity * multiplyingFactor

    }

    //Will parse the set and return the number of sheets
    //a set is something like 25r350x2
    private fun parseSet(setString: String): Int {

        val splits = setString.split("x", ignoreCase = true)
        if (splits.isEmpty())
            return 0

        if (splits.size > 2)
            throw IllegalArgumentException("Illegal string format detected")

        return  if (splits.size == 2)
            parseUnit(splits[0]) * splits[1].toInt()
        else
            parseUnit(splits[0])
    }

    fun totalSheets():Int{
        if(!isValid)
            return -1

        var totalSheets=0
        for (set in parseStringToSets()){
            totalSheets += parseSet(set)
        }

        return totalSheets
    }

}