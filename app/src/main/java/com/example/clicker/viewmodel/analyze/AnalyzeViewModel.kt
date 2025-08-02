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

    private val _listChartLiveData: MutableLiveData<ArrayList<Entry>> =
        MutableLiveData(arrayListOf(Entry(0f, 0f)))
    val listChartLiveData: LiveData<ArrayList<Entry>> get() = _listChartLiveData


    private val _stopActivityVideoSecond: MutableLiveData<Int> = MutableLiveData(0)
    val stopActivityVideoSecond: LiveData<Int> get() = _stopActivityVideoSecond

    private val _scoredText: MutableLiveData<String> = MutableLiveData()
    val scoredText: LiveData<String> get() = _scoredText

    val videoInfo: MutableLiveData<ClickVideoListWithClickInfo?> = MutableLiveData(null)
    val clickInfo: MutableLiveData<List<ClickInfo>> = MutableLiveData(listOf())
    val videoId: MutableLiveData<String> = MutableLiveData()

    private val _readAllData: MutableLiveData<List<ClickVideoListWithClickInfo>> =
        MutableLiveData();
    val readAllData: LiveData<List<ClickVideoListWithClickInfo>> get() = _readAllData

    init {
        // MainActivity에서 데이터를 전달받지 않은 경우에만 데이터베이스에서 가져오기
        // setVideo 메서드에서 데이터가 설정되면 데이터베이스 조회는 필요하지 않음
    }

    fun initializeFromDatabase() {
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.getAll().collect {
                withContext(Dispatchers.Main) {
                    if (it.isNotEmpty()) {
                        Log.d(TAG, "getAll: ${it.last().clickInfoList}")
                    } else {
                        Log.d(TAG, "getAll: No data in database")
                    }
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


    private fun startTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            val secondList = clickInfoToSecondList()
            while (true) {
                val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
                if (tracker.state == PlayerConstants.PlayerState.PLAYING
                ) {
                    val clickInfoList = videoInfo.value?.clickInfoList
                    if (clickInfoList != null && secondList.isNotEmpty()) {
                        val finalIndex = if (second < secondList[0]) {
                            // 첫 번째 클릭 전인 경우
                            -1
                        } else {
                            // 현재 시간이 어느 클릭 시간에 해당하는지 직접 계산
                            var index = -1
                            for (i in secondList.indices) {
                                if (second >= secondList[i]) {
                                    index = i // 현재 시간 이하인 마지막 클릭 인덱스
                                } else {
                                    break
                                }
                            }
                            index
                        }
                        
                        if (finalIndex == -1) {
                            // 첫 번째 클릭 전 상태 - 초기값 유지
                            _nowPosition.postValue(-1)
                            withContext(Dispatchers.Main){
                                _scoredText.value = "0 0 0"
                            }
                            Log.d(TAG, "startTracking: Before first click, second=$second, showing initial state")
                        } else if (finalIndex >= 0 && finalIndex < clickInfoList.size) {
                            val currentIndex = clickInfoList[finalIndex]
                            
                            Log.d(TAG, "startTracking: second=$second, finalIndex=$finalIndex, total_size=${clickInfoList.size}, isLast=${finalIndex == clickInfoList.size - 1}")
                            Log.d(TAG, "startTracking: clickSecond=${currentIndex.clickSecond}, scores=${currentIndex.plus} ${currentIndex.minus} ${currentIndex.total}")
                            
                            _nowPosition.postValue(finalIndex)
                            withContext(Dispatchers.Main){
                                _scoredText.value = "${currentIndex.plus} ${currentIndex.minus} ${currentIndex.total}"
                            }
                        } else {
                            Log.e(TAG, "startTracking: Index out of bounds! finalIndex=$finalIndex, listSize=${clickInfoList.size}")
                        }
                    } else {
                        // 클릭 정보가 없거나 비어있는 경우
                        _nowPosition.postValue(-1)
                        withContext(Dispatchers.Main){
                            _scoredText.value = "0 0 0"
                        }
                        Log.d(TAG, "startTracking: No click info available")
                    }
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
        //_scoredText.value = videoInfo.value!!.plusScore.toString() + " " + videoInfo.value!!.minusScore.toString() + " " + videoInfo.value!!.totalScore.toString()
        clickInfo.value = data.clickInfoList
        videoId.value = data.videoId
        dataToEntry()
        startTracking()
        startAddDataEntry()
    }
    
    fun insertVideoData(success: () -> Unit, failed: () -> Unit) {
        val currentVideoInfo = videoInfo.value
        if (currentVideoInfo != null && videoId.value?.isNotEmpty() == true) {
            viewModelScope.launch {
                try {
                    // 현재 상태의 데이터로 업데이트하여 저장
                    val updatedVideoInfo = currentVideoInfo.copy(
                        clickInfoList = clickInfo.value ?: emptyList()
                    )
                    
                    databaseRepository.insert(updatedVideoInfo)
                    
                    withContext(Dispatchers.Main) {
                        success()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to insert video data", e)
                    withContext(Dispatchers.Main) {
                        failed()
                    }
                }
            }
        } else {
            failed()
        }
    }
}
