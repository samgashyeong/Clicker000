package com.example.clicker.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.remote.model.youtube.Item
import com.example.clicker.data.repository.YoutubeServiceRepository
import com.example.clicker.util.Utils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainViewModel(val urlString : MutableLiveData<String>,
                    val plus : MutableLiveData<Int>,
                    val minus : MutableLiveData<Int>,
                    val total : MutableLiveData<Int>,
                    val startPoint : MutableLiveData<Float?>,
                    val clickInfo : MutableLiveData<ArrayList<ClickInfo>>,
                    val tracker: YouTubePlayerTracker,
                    var testString : MutableLiveData<String>
                    ) : ViewModel() {

    var videoInfo : MutableLiveData<Item> = MutableLiveData()
    private val youtubeServiceRepository : YoutubeServiceRepository = YoutubeServiceRepository()

    fun getVideoInfo(id : String){
        viewModelScope.launch(Dispatchers.IO) {
            videoInfo.postValue(youtubeServiceRepository.searchYoutubeInfo("snippet", id, Utils.youtubeDataApiKey))
        }
    }

    fun minusPoint(view : View){
        minus.value = minus.value?.plus(-1)
        total.value = total.value?.plus(-1)

        testString.value += "-1, ${tracker.currentSecond}초\n"
        clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = -1, null, plus.value!!, minus.value!!, total.value!!))
    }

    fun plusPoint(view : View){
        plus.value = plus.value?.plus(1)
        total.value = total.value?.plus(1)

        testString.value += "+1, ${tracker.currentSecond}초\n"
        clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = +1, null, plus.value!!, minus.value!!, total.value!!))
    }


    fun saveData(view : View){

    }
    fun extractYouTubeVideoId(url: String): MutableLiveData<String> {
        val basePart = url.substringAfterLast( "v=")
        return MutableLiveData(basePart.substringBefore("&si="))
    }

}