package com.tatsuo.bodytemperature.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Person constructor (
    @PrimaryKey(autoGenerate = true)
    var personId: Int = 0,
    var name: String = "",
    var order: Int = 0
)
