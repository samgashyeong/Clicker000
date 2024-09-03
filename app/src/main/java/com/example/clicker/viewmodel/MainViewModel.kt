package com.example.clicker.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.remote.model.youtube.Item
import com.example.clicker.data.repository.YoutubeServiceRepository
import com.example.clicker.util.RankingDto
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
                    val tracker: YouTubePlayerTracker,
                    private val youtubeServiceRepository : YoutubeServiceRepository
                    ) : ViewModel() {
    val clickInfo : MutableLiveData<ArrayList<ClickInfo>> = MutableLiveData(ArrayList())
    val stopActivityVideoSecond : MutableLiveData<Int> = MutableLiveData(0)
    val isStartVideo : MutableLiveData<Boolean> = MutableLiveData(false)
    val ranking : MutableLiveData<ArrayList<RankingDto>> = MutableLiveData(ArrayList())


    val urlString : MutableLiveData<String> = MutableLiveData("")
    val plus : MutableLiveData<Int> = MutableLiveData(0)
    val minus : MutableLiveData<Int?> = MutableLiveData(0)
    val total : MutableLiveData<Int> = MutableLiveData(0)
    var videoInfo : MutableLiveData<Item> = MutableLiveData()
    var youTubePlayer : MutableLiveData<YouTubePlayer> = MutableLiveData()

    val startPoint : MutableLiveData<Float?> = MutableLiveData(null)
    val vib : MutableLiveData<Boolean> = MutableLiveData( false)

    fun getVideoInfo(id : String, key : String){
        viewModelScope.launch(Dispatchers.IO) {
            videoInfo.postValue(youtubeServiceRepository.searchYoutubeInfo("snippet", id, key))
        }
    }

    init {
        //ranking.value = generateDummyData() as ArrayList<RankingDto>
    }

    fun generateDummyData(): List<RankingDto> {
        val names = listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hank", "Ivy", "Jack",
            "Kara", "Liam", "Mia", "Nina", "Oscar", "Paul", "Quincy", "Rose", "Sam", "Tina",
            "Uma", "Vince", "Wendy", "Xander", "Yara", "Zane", "Aaron", "Bianca", "Cody", "Diana",
            "Ethan", "Fiona", "George", "Holly", "Ian", "Jenny", "Kevin", "Lara", "Mike", "Nora",
            "Olivia", "Pete", "Quinn", "Rita", "Steve", "Tara", "Ursula", "Victor", "Will", "Zara")

        return List(50) { index ->
            val plus = Random.nextInt(0, 100)
            val minus = Random.nextInt(0, 100)
            val total = plus - minus
            RankingDto(
                name = names[index % names.size],
                plus = plus,
                minus = minus,
                total = total
            )
        }
    }

    fun addPlayer(data : RankingDto, callBack : () -> Unit){
        ranking.value!!.add(data)
        val updateData = ranking.value!!
        updateData.sortWith(Comparator<RankingDto>{ a, b ->
            if(a.total == b.total){
                a.name.compareTo(b.name)
            }
            else{
                a.total.compareTo(b.total)
            }
        })
        updateData.reverse()

        ranking.value = updateData
        plus.value = 0;
        minus.value = 0;
        total.value = 0

        callBack()
    }

    fun convertDataToText(): String {
        return ranking.value!!.mapIndexed { index, rankingDto ->
            "${index + 1}. ${rankingDto.name} : ${rankingDto.plus} ${rankingDto.minus} ${rankingDto.total}"
        }.joinToString("\n")
    }

    fun rightButton(view : View, isChangeButton : MutableLiveData<Boolean>, isVib : MutableLiveData<Boolean>){
        if(isChangeButton.value == true){
            minus.value = minus.value?.plus(1)
            total.value = total.value?.plus(1)

            Log.d(TAG, "plusPoint: ${tracker.currentSecond}")
            clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = +1, null, minus.value!!, plus.value!!, total.value!!))
        }
        else{
            minus.value = minus.value?.plus(-1)
            total.value = total.value?.plus(-1)

            clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = -1, null, plus.value!!, minus.value!!, total.value!!))
        }

        if(isVib.value == true && vib.value == false){
            vib.value = true
        }
    }

    fun leftButton(view : View, isChangeButton : MutableLiveData<Boolean>, isVib : MutableLiveData<Boolean>){

        if(isChangeButton.value == true){
            plus.value = plus.value?.plus(-1)
            total.value = total.value?.plus(-1)

            clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = -1, null, minus.value!!, plus.value!!, total.value!!))
        }
        else{
            plus.value = plus.value?.plus(1)
            total.value = total.value?.plus(1)

            clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = +1, null, plus.value!!, minus.value!!, total.value!!))
        }

        if(isVib.value == true && vib.value == false){
            vib.value = true
        }
    }
    fun extractYouTubeVideoId(url: String): MutableLiveData<String> {
        val basePart = url.substringAfterLast( "v=")
        return MutableLiveData(basePart.substringBefore("&si="))
    }


    fun swapPlusAndMinus(){
        val tmp = plus.value
        plus.value = minus.value
        minus.value = tmp
    }

    fun clearRankingData() {
        val updateData = ranking.value!!

        updateData.clear()
        ranking.value = updateData
    }
}