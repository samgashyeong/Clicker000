package com.example.clicker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

//@Entity(tableName = "clickInfo")

data class ClickInfo(
    var clickSecond : Float,
    var clickScorePoint : Int,
    var clickDescription : String?,
)
