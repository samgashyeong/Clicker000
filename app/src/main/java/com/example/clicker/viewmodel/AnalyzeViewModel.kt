package com.example.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.github.mikephil.charting.data.Entry
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnalyzeViewModel @Inject constructor(
                                           val tracker: YouTubePlayerTracker) : ViewModel() {

    val videoInfo: MutableLiveData<ClickVideoListWithClickInfo?> = MutableLiveData(null)
    val clickInfo : MutableLiveData<List<ClickInfo>> = MutableLiveData()
    val videoId : MutableLiveData<String> = MutableLiveData()
    val nowPosition : MutableLiveData<Int> = MutableLiveData(0)

    val youtubePlayer : MutableLiveData<YouTubePlayer> = MutableLiveData()

    fun dataToEntry(): ArrayList<Entry>{
        val listEntry : ArrayList<Entry> = ArrayList()
        var totalScore = 0
        listEntry.add(Entry(0f, 0f))
        for(i in clickInfo.value!!){
            totalScore += i.clickScorePoint
            listEntry.add(Entry(i.clickSecond, totalScore.toFloat()))
        }
        return listEntry
    }


    private fun clickInfoToSecondList() = clickInfo.value!!.map {
        String.format("%.1f", it.clickSecond.toDouble()).toDouble()
    }

    fun startTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            val secondList = clickInfoToSecondList()

            while(true){
                val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
                //Log.d(TAG, "startTracking: ${second}")
                if(tracker.state == PlayerConstants.PlayerState.PLAYING && secondList.contains(second)){
                    nowPosition.postValue(secondList.indexOf(second))
                }
                delay(100L)
            }
        }
    }
}