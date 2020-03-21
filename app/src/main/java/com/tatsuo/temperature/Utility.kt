package com.tatsuo.temperature

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
