package com.tatsuo.bodytemperature.db

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        if (value != null) {
            return Date(value)
        } else {
            return Date(0L)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        if (date != null) {
            return date.time
        } else {
            return 0L
        }
    }
}