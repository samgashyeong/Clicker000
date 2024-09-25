package com.example.clicker.viewmodel.analyze

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.util.lowerBound
import com.github.mikephil.charting.data.Entry
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    val tracker: YouTubePlayerTracker,
    private val databaseRepository: ClickVideoRepository
) : ViewModel() {

    val youtubePlayer: MutableLiveData<YouTubePlayer> = MutableLiveData()

    private val listEntry: ArrayList<Entry> = arrayListOf(Entry(0f, 0f))

    private val _nowPosition: MutableLiveData<Int> = MutableLiveData(0)
    val nowPosition: LiveData<Int> get() = _nowPosition

    val _listChartLiveData: MutableLiveData<ArrayList<Entry>> =
        MutableLiveData(arrayListOf(Entry(0f, 0f)))
    val listChartLiveData: LiveData<ArrayList<Entry>> get() = _listChartLiveData


    val scoredText: MutableLiveData<String> = MutableLiveData()
    val videoInfo: MutableLiveData<ClickVideoListWithClickInfo?> = MutableLiveData(null)
    val clickInfo: MutableLiveData<List<ClickInfo>> = MutableLiveData(listOf())
    val videoId: MutableLiveData<String> = MutableLiveData()

    private val _readAllData: MutableLiveData<List<ClickVideoListWithClickInfo>> =
        MutableLiveData();
    val readAllData: LiveData<List<ClickVideoListWithClickInfo>> get() = _readAllData

    init {
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.getAll().collect {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "getAll: ${it.last().clickInfoList}")
                    _readAllData.value = it
                }
            }
        }
    }

    fun update(clickVideoListWithClickInfo: ClickVideoListWithClickInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.update(clickVideoListWithClickInfo)
        }
    }

    fun dataToEntry() {
        for (i in clickInfo.value!!) {
            listEntry.add(Entry(i.clickSecond, i.total.toFloat()))
        }
    }

    private fun clickInfoToSecondList() = clickInfo.value!!.map {
        String.format("%.1f", it.clickSecond.toDouble()).toDouble()
    }


    fun startTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            val secondList = clickInfoToSecondList()
            while (true) {
                val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
                if (tracker.state == PlayerConstants.PlayerState.PLAYING && secondList.contains(
                        second
                    )
                ) {
                    val result = lowerBound(secondList, second)
                    _nowPosition.postValue(result)
                }
                delay(100L)
            }
        }
    }

    fun startAddDataEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (tracker.state == PlayerConstants.PlayerState.PLAYING) {

                    val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()


                    var result =
                        lowerBound(clickInfo.value!!.map { it.clickSecond.toDouble() }, second)

                    Log.d(TAG, "startAddDataEntry: ${result} ${clickInfo}")
                    if (result == clickInfo.value!!.size - 1) {
                        result += 1
                    }
                    withContext(Dispatchers.Main) {
                        _listChartLiveData.value = ArrayList(listEntry.subList(0, result + 1))
                    }
                    delay(300L)
                }
            }
        }
    }

    fun setVideo(data: ClickVideoListWithClickInfo) {
        videoInfo.value = data!!
        scoredText.value =
            videoInfo.value!!.plusScore.toString() + " " + videoInfo.value!!.minusScore.toString() + " " + videoInfo.value!!.totalScore.toString()
        clickInfo.value = data.clickInfoList
        videoId.value = data.videoId
        dataToEntry()
        startTracking()
        startAddDataEntry()
    }
}