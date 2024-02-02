package com.example.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.github.mikephil.charting.data.Entry
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class AnalyzeViewModel(val videoInfo: MutableLiveData<ClickVideoListWithClickInfo>,
                       val clickInfo : MutableLiveData<List<ClickInfo>>,
    val videoId : MutableLiveData<String>,
    val tracker: YouTubePlayerTracker,
    val nowPosition : Int) : ViewModel() {

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
}