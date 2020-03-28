package com.tatsuo.bodytemperature.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Temperature::class, Person::class), version = 1)
@TypeConverters(DateConverter::class)
public abstract class TemperatureDatabase: RoomDatabase() {
    abstract fun temperatureDao(): TemperatureDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: TemperatureDatabase? = null

        fun getDatabase(context: Context): TemperatureDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        TemperatureDatabase::class.java,
                        "temperature_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
