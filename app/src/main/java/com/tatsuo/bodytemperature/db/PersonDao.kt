package com.tatsuo.bodytemperature.db

import androidx.room.*

@Dao
interface PersonDao {

    @Query("SELECT * FROM Person")
    fun getAllPerson() : List<Person>

    @Query("SELECT * from person where personId = :id LIMIT 1")
    fun loadPersonById(id: Int): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(person: Person)

    @Update
    fun update(person: Person)

    @Delete
    fun delete(person: Person)

    @Query("SELECT COUNT(*) FROM person")
    fun count(): Long

}