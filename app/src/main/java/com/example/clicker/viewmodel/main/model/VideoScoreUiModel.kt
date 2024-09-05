package com.example.clicker.viewmodel.main.model

import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.remote.model.youtube.Item
import javax.annotation.concurrent.Immutable


@Immutable
data class VideoScoreUiModel(
    val plus : Int = 0,
    val minus : Int = 0,
    val total : Int = 0,
    val leftText : String = "0",
    val rightText : String = "0",
    val url : String = "",
    val startPoint : Float = 0f,
    val clickInfoList : ArrayList<ClickInfo> = arrayListOf(),
    val videoInfo : Item?,
)