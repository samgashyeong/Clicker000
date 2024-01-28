package com.example.clicker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clickInfo")
data class ClickInfo(
    @PrimaryKey(autoGenerate = true)
    var clickSecond : Float,
    var clickScorePoint : Int,
    var clickDescription : String?,
)
