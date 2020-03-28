package com.tatsuo.bodytemperature.db

import androidx.room.*
import java.util.*

@Dao
interface TemperatureDao {
    @Query("SELECT * from temperatures where personId = :targetPersonId order by date desc")
    fun getAllTemperatureData(targetPersonId: Int): List<Temperature>

    @Query("SELECT * from temperatures where personId = :targetPersonId and date >= :beforeDate and date <= :afterDate order by date desc")
    fun getTemperatureData(targetPersonId: Int, beforeDate: Date, afterDate: Date): List<Temperature>

    @Query("SELECT * from temperatures where id = :id LIMIT 1")
    fun loadTemperatureById(id: Long): List<Temperature>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 追加処理　コンフリクトが起こった時は置き換える
    fun insert(temperature: Temperature)

    @Update
    fun update(temperature: Temperature)

    @Delete
    fun delete(temperature: Temperature)

    @Query("SELECT COUNT(*) FROM temperatures")
    fun count(): Long
}
