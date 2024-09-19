package com.example.clicker.viewmodel.analyze

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.util.lowerBound
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
class AnalyzeViewModel @Inject constructor(val tracker: YouTubePlayerTracker) : ViewModel() {

    val videoInfo: MutableLiveData<ClickVideoListWithClickInfo?> = MutableLiveData(null)
    val clickInfo : MutableLiveData<List<ClickInfo>> = MutableLiveData(listOf())
    val videoId : MutableLiveData<String> = MutableLiveData()
    val nowPosition : MutableLiveData<Int> = MutableLiveData(0)

    val youtubePlayer : MutableLiveData<YouTubePlayer> = MutableLiveData()
    private val listEntry : ArrayList<Entry> = arrayListOf(Entry(0f, 0f))


    val listChartLiveData : MutableLiveData<ArrayList<Entry>> = MutableLiveData(arrayListOf(Entry(0f, 0f)))

    var nowChartIndex : Int = 1
    val checkEntry : ArrayList<Boolean> = arrayListOf(true)
    var checkList : ArrayList<Entry> = ArrayList()

    val scoredText : MutableLiveData<String> =  MutableLiveData()

    fun dataToEntry(){
        for(i in clickInfo.value!!){
            listEntry.add(Entry(i.clickSecond, i.total.toFloat()))
            checkEntry.add(false)
        }
    }

    private fun clickInfoToSecondList() = clickInfo.value!!.map {
        String.format("%.1f", it.clickSecond.toDouble()).toDouble()
    }

    private fun clickInfoToSecondIntList() = clickInfo.value!!.map {
        it.clickSecond.toInt()
    }

    fun startTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            val secondList = clickInfoToSecondList()
            while(true){
                val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
                if(tracker.state == PlayerConstants.PlayerState.PLAYING && secondList.contains(second)){
                    nowPosition.postValue(secondList.indexOf(second))
                }
                delay(100L)
            }
        }
    }

    fun startAddDataEntry(){
        viewModelScope.launch(Dispatchers.IO) {
            val secondList = clickInfoToSecondList()
            while(true){
                if(tracker.state == PlayerConstants.PlayerState.PLAYING){
//                    val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
//
//                    val checkList_ : ArrayList<Entry> = ArrayList()
//
//                    for(i in nowChartIndex..<listEntry.size){
//                        if(listEntry[i].x <= second && !checkEntry[i]){
//                            checkList_.add(listEntry[i])
//                        }
//                    }
//                    checkList = checkList_
//
//                    if(checkList.size != 0){
//                        for(i in nowChartIndex..<nowChartIndex + checkList.size){
//                            checkEntry[i] = true
//                        }
//                        Log.d(TAG, "startAddDataEntry: ${checkEntry}")
//                        nowChartIndex += checkList.size
//                    }
//                    Log.d(TAG, "startAddDataEntry: ${nowChartIndex} ${checkList}")
//
//                    listChartData.addAll(checkList)
//                    listChartLiveData.postValue(checkList)

                    val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()


                    val result = lowerBound(clickInfo.value!!.map { it.clickSecond }, second.toFloat())

                    Log.d(TAG, "startAddDataEntry: ${result}")
                    delay(300L)
                }
            }
        }
    }
}