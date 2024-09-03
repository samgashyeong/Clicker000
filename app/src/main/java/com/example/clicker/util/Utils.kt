package com.example.clicker.util

import com.example.clicker.viewmodel.Mode

object Utils {

    const val youtubeUrl = "https://youtube.googleapis.com/youtube/v3/"
}

val intToMode : Map<Int, Mode> = mapOf(
    0 to Mode.Default(),
    1 to Mode.Ranking()
)
val modeToInt : Map<Mode, Int> = mapOf(
    Mode.Default() to 0,
    Mode.Ranking() to 1
)

data class RankingDto(
    val name : String,
    val plus : Int,
    val minus : Int,
    val total : Int
)
