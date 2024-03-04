package com.clicker000.clicker.data.database

import java.io.Serializable

//@Entity(tableName = "clickInfo")

data class ClickInfo(
    var clickSecond : Float,
    var clickScorePoint : Int,
    var clickDescription : String?,
    var plus : Int,
    var minus : Int,
    var total : Int
): Serializable
