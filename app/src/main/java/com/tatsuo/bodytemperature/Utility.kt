package com.tatsuo.bodytemperature

import android.util.Log
import java.util.*

fun getUUID(): String {
    return UUID.randomUUID().toString()
}

fun getConditionIdList(conditions : String) : List<Int> {
    val conditionIdList = mutableListOf<Int>()

    val conditionIdStrList = conditions.split(",")
    for(conditionIdStr in conditionIdStrList){
        try {
            val conditionId = conditionIdStr.toInt()
            conditionIdList.add(conditionId)
        } catch (nfe : NumberFormatException ){}
    }

    return conditionIdList
}

fun getDateFormatString() : String {
    val locale = Locale.getDefault()
    val language = locale.language
    // val country = locale.country
    // Log.e("***","Langage : "+language+"  Country : "+country)

    var dateFormatString = "E, MMM d h:mm a"
    if (language == "ja") {
        dateFormatString = "M'月'd'日('E')' H:mm"
    } else if(language == "es"){
        dateFormatString = "E d MMM h:mm a"
    }

    return dateFormatString
}

fun getDateFormatStringMMDD() : String {
    val locale = Locale.getDefault()
    val language = locale.language

    var dateFormatString = "E, MMM d"
    if (Locale.getDefault().equals(Locale.JAPAN)) {
        dateFormatString = "M'月'd'日('E')'"
    } else if(language == "es"){
        dateFormatString = "E d MMM"
    }

    return dateFormatString
}

fun getTimeFormatString() : String {
    val locale = Locale.getDefault()
    val language = locale.language

    var timeFormatString = "h:mm a"
    if (Locale.getDefault().equals(Locale.JAPAN)) {
        timeFormatString = "H:mm"
    }

    return timeFormatString
}

fun convertCommaToPeriod(source : String) : String {
    Log.e("***","convert : "+source+" -> "+source.replace(",", "."))
    return source.replace(",", ".")
}
