package com.example.clicker.data.database

import android.content.ClipData.Item
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clickVideoList")
data class ClickVideoList(
    @PrimaryKey(autoGenerate = true)
    val clickVideoListID : Int,
    val videoInfo : Item,
    val videoId : String,
    val plusScore : Int,
    val minusScore : Int,
    val totalScore : Int,
)