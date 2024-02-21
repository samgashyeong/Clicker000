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
import com.example.clicker.util.Utils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
                    val tracker: YouTubePlayerTracker,
                    val youtubeServiceRepository : YoutubeServiceRepository
                    ) : ViewModel() {

    val urlString : MutableLiveData<String> = MutableLiveData("")
    val plus : MutableLiveData<Int> = MutableLiveData(0)
    val minus : MutableLiveData<Int> = MutableLiveData(0)
    val total : MutableLiveData<Int> = MutableLiveData(0)
    val startPoint : MutableLiveData<Float?> = MutableLiveData(null)
    var testString : MutableLiveData<String> = MutableLiveData("")
    val clickInfo : MutableLiveData<ArrayList<ClickInfo>> = MutableLiveData(ArrayList())
    var videoInfo : MutableLiveData<Item> = MutableLiveData()

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
        Log.d(TAG, "plusPoint: ${tracker.currentSecond}")
        clickInfo.value!!.add(ClickInfo(clickSecond = tracker.currentSecond, clickScorePoint = +1, null, plus.value!!, minus.value!!, total.value!!))
    }


    fun saveData(view : View){

    }
    fun extractYouTubeVideoId(url: String): MutableLiveData<String> {
        val basePart = url.substringAfterLast( "v=")
        return MutableLiveData(basePart.substringBefore("&si="))
    }

}