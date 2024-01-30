package com.example.clicker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(tableName = "clickVideoList")
data class ClickVideo(
    val videoInfo : com.example.clicker.data.remote.model.youtube.Item,
    val videoId : String,
    val plusScore : Int,
    val minusScore : Int,
    val totalScore : Int,
){
    @PrimaryKey(autoGenerate = true)
    val clickVideoListID : Int = 0
}