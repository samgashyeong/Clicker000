package com.example.clicker.util


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

val CLICKER000_EXTERNAL_FILE_NAME = "clicker000"

data class RankingDto(
    val name : String,
    val plus : Int,
    val minus : Int,
    val total : Int
)
sealed class Mode(){
    data class Default(val name : String = "Default") : Mode()
    data class Ranking(val name : String = "Ranking") : Mode()
}

fun lowerBound(list : List<Double>, data : Double) : Int{
    var left = 0
    var right = list.size-1
    var answer = list.size-1;
    while(left<=right){
        val mid = (left+right)/2;

        if(list[mid] >= data){
            right = mid-1;
            answer = mid;
        }
        else{
            left = mid+1;
        }
    }

    return answer
}
