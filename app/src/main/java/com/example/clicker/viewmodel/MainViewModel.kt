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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
                    val tracker: YouTubePlayerTracker,
                    val youtubeServiceRepository : YoutubeServiceRepository
                    ) : ViewModel() {

    val urlString : MutableLiveData<String> = MutableLiveData("")
    val plus : MutableLiveData<Int> = MutableLiveData(0)
    val minus : MutableLiveData<Int?> = MutableLiveData(0)
    val total : MutableLiveData<Int> = MutableLiveData(0)
    val startPoint : MutableLiveData<Float?> = MutableLiveData(null)
    val clickInfo : MutableLiveData<ArrayList<ClickInfo>> = MutableLiveData(ArrayList())
    var videoInfo : MutableLiveData<Item> = MutableLiveData()
    var youTubePlayer : MutableLiveData<YouTubePlayer> = MutableLiveData()
    val stopActivityVideoSecond : MutableLiveData<Int> = MutableLiveData(0)
    val isStartVideo : MutableLiveData<Boolean> = MutableLiveData(false)

    val vib : MutableLiveData<Boolean> = MutableLiveData(false)

    fun getVideoInfo(id : String, key : String){
        viewModelScope.launch(Dispatchers.IO) {
            videoInfo.postValue(youtubeServiceRepository.searchYoutubeInfo("snippet", id, key))
        }
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
}