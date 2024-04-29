package com.example.clicker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "ClickVideoInfo")
data class ClickVideoListWithClickInfo(
    val videoInfo : com.example.clicker.data.remote.model.youtube.Item,
    val startPoint : Int,
    val videoId : String,
    val plusScore : Int,
    val minusScore : Int,
    val totalScore : Int,
    var clickInfoList : List<ClickInfo>
): Serializable{
    @PrimaryKey(autoGenerate = true)
    var clickVideoListID : Int = 0
}

