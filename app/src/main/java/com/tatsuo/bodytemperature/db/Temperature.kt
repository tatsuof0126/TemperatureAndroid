package com.tatsuo.bodytemperature.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tatsuo.bodytemperature.getUUID
import java.util.*


@Entity(tableName = "temperatures")
data class Temperature constructor(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var uuid: String = getUUID(),
        var date: Date = Date(),
        var personId: Int = 1,
        var temperature: Double = 0.0,
        var useAntipyretic: Int = 0,
        var conditions: String = "",
        var memo: String = ""
){

        fun getFahrenheitTemperature() : Double {
                return temperature * 9f / 5f + 32f
        }

        fun setTemperatureFromFahrenheit(fahrenheitTemperature : Double) {
                temperature = (fahrenheitTemperature - 32f) * 5f / 9f
        }

        fun getConditionString() : String {
                val conditionString = StringBuilder()

                val conditionIdList = conditions.split(",")

                for(conditionIdStr in conditionIdList){
                        try {
                                val conditionId = conditionIdStr.toInt()
                                conditionString.append(ConditionList.conditionList[conditionId].text)
                                conditionString.append(", ")
                        } catch (nfe : NumberFormatException ){}
                }
                if(conditionString.length >= 2) {
                        conditionString.delete(conditionString.length-2, conditionString.length)
                }

                return conditionString.toString()
        }

        fun getTemperatureString() : String {
                return id.toString() + " : " + personId + " : " + date + " : " + temperature + " : " + useAntipyretic + " : " + conditions + " : " + uuid + " : " + memo
        }

}
