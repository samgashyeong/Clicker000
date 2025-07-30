package com.example.clicker.viewmodel.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.ExternalStorageRepository
import com.example.clicker.data.repository.SettingRepository
import com.example.clicker.data.repository.YoutubeServiceRepository
import com.example.clicker.util.ApiKeyProvider
import com.example.clicker.util.Mode
import com.example.clicker.util.RankingDto
import com.example.clicker.util.VibrationProvider
import com.example.clicker.util.intToMode
import com.example.clicker.util.modeToInt
import com.example.clicker.viewmodel.main.model.SettingUiModel
import com.example.clicker.viewmodel.main.model.VideoScoreUiModel
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val tracker: YouTubePlayerTracker,
    private val vibrationProvider: VibrationProvider,
    private val clickVideoRepository: ClickVideoRepository,
    private val settingRepository: SettingRepository,
    private val youtubeServiceRepository: YoutubeServiceRepository,
    private val apiKeyProvider: ApiKeyProvider,
    private val externalStorageRepository: ExternalStorageRepository
) : ViewModel() {
    private val _videoScoreUiModel: MutableLiveData<VideoScoreUiModel> =
        MutableLiveData(VideoScoreUiModel(videoInfo = null))
    val videoScoreUiModel: LiveData<VideoScoreUiModel> get() = _videoScoreUiModel

    private val _settingUiModel: MutableLiveData<SettingUiModel> = MutableLiveData(SettingUiModel())
    val settingUiModel: LiveData<SettingUiModel> get() = _settingUiModel

    private val _stopActivityVideoSecond: MutableLiveData<Int> = MutableLiveData(0)
    val stopActivityVideoSecond: LiveData<Int> get() = _stopActivityVideoSecond

    private val _ranking: MutableLiveData<ArrayList<RankingDto>> = MutableLiveData(ArrayList())
    val ranking: LiveData<ArrayList<RankingDto>> get() = _ranking


    init {
        getSettingData()
    }

    fun addPlayer(data: RankingDto, callBack: () -> Unit) {
        _ranking.value!!.add(data)
        val updateData = _ranking.value!!
        updateData.sortWith(Comparator<RankingDto> { a, b ->
            if (a.total == b.total) {
                a.name.compareTo(b.name)
            } else {
                a.total.compareTo(b.total)
            }
        })
        updateData.reverse()

        _ranking.value = updateData
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(
            plus = 0,
            minus = 0,
            total = 0,
            leftText = "0",
            rightText = "0"
        )

        callBack()
    }

    private fun writeDataFile(externalFileDate: String) {
        viewModelScope.launch {
            val dataList = clickVideoRepository.getAll().first()
            val jsonString = Gson().toJson(dataList)
            externalStorageRepository.findClickFile(jsonString, externalFileDate)
        }
    }

    fun convertDataToText(): String {

        return ranking.value!!.mapIndexed { index, rankingDto ->
            "${index + 1}. ${rankingDto.name} : ${rankingDto.plus} ${rankingDto.minus} ${rankingDto.total}"
        }.joinToString("\n")
    }


    private fun saveExternalFileDate() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDateTime = LocalDateTime.now() // This includes both date and time
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            settingRepository.saveExternalFileDate(currentDateTime.format(formatter))
            withContext(Dispatchers.Main){
                _settingUiModel.postValue(
                    _settingUiModel.value!!.copy(
                        externalFileDate = currentDateTime.format(formatter)
                    )
                )
            }
        }
    }

    fun clearScoreData() {
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(
            plus = 0,
            minus = 0,
            total = 0,
            leftText = "0",
            rightText = "0"
        )
    }

    fun saveIsChangeButton(isSwitchOn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveIsChangeButton(isSwitchOn)
        }
    }

    fun saveIsVibButton(isSwitchOn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveIsVibButton(isSwitchOn)
        }
    }

    fun saveIsSetStartPoint(isSwitchOn: Boolean){
        viewModelScope.launch(Dispatchers.IO){
            settingRepository.saveSetStartPoint(isSwitchOn)
        }
    }

    fun clearClickInfo() {
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(clickInfoList = arrayListOf())
    }

    fun saveMode(mode: Mode) {
        Log.d(TAG, "saveMode: ${mode.toString()}")
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveMode(modeToInt[mode]!!)
        }
    }

    fun setMode(mode: Mode) {
        _settingUiModel.value = _settingUiModel.value!!.copy(mode = mode)
        saveMode(mode)
    }

    fun changeStartPoint(startPoint: Float) {
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(startPoint = startPoint)
    }

    private fun plus() {
        val value = videoScoreUiModel.value!!.plus
        val totalValue = videoScoreUiModel.value!!.total
        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(plus = value + 1, total = totalValue + 1)

        Log.d(TAG, "onStop: ${tracker.currentSecond.toInt()}")
        val updateList = videoScoreUiModel.value?.clickInfoList
        updateList!!.add(
            ClickInfo(
                clickSecond = tracker.currentSecond,
                clickScorePoint = +1,
                null,
                videoScoreUiModel.value!!.plus,
                videoScoreUiModel.value!!.minus,
                videoScoreUiModel.value!!.total
            )
        )
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(clickInfoList = updateList)
    }


    private fun minus() {
        val totalValue = videoScoreUiModel.value!!.total
        val value = videoScoreUiModel.value!!.minus
        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(minus = value - 1, total = totalValue - 1)
        val updateList = videoScoreUiModel.value?.clickInfoList
        updateList!!.add(
            ClickInfo(
                clickSecond = tracker.currentSecond,
                clickScorePoint = -1,
                null,
                videoScoreUiModel.value!!.plus,
                videoScoreUiModel.value!!.minus,
                videoScoreUiModel.value!!.total
            )
        )
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(clickInfoList = updateList)
    }

    fun changeStopPoint(data: Int) {
        _stopActivityVideoSecond.value = data
    }

    fun extractYouTubeVideoId(url: String) {
        var basePart = url.substringAfterLast("v=")
        basePart = basePart.substringBefore("&si=")

        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(videoId = basePart)
    }

    fun rightButton() {
        vibrationProvider.triggerVibration()
        if (settingUiModel.value!!.isChangeButton == false) {
            minus()
            _videoScoreUiModel.value =
                _videoScoreUiModel.value?.copy(rightText = videoScoreUiModel.value!!.minus.toString())
        } else {
            plus()
            _videoScoreUiModel.value =
                _videoScoreUiModel.value?.copy(rightText = videoScoreUiModel.value!!.plus.toString())
        }
    }

    fun leftButton() {
        vibrationProvider.triggerVibration()
        if (settingUiModel.value!!.isChangeButton == false) {
            plus()
            _videoScoreUiModel.value =
                _videoScoreUiModel.value?.copy(leftText = videoScoreUiModel.value!!.plus.toString())
        } else {
            minus()
            _videoScoreUiModel.value =
                _videoScoreUiModel.value?.copy(leftText = videoScoreUiModel.value!!.minus.toString())
        }
    }

    fun getVideoInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val videoInfo = youtubeServiceRepository.searchYoutubeInfo(
                "snippet",
                videoScoreUiModel.value!!.videoId,
                apiKeyProvider.getApiKey()
            )
            withContext(Dispatchers.Main) {
                _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(videoInfo = videoInfo)
            }
            Log.d(TAG, "getVideoInfo: ${videoInfo}")
        }
    }


    fun leftMinusRightPlus() {
        val plus = videoScoreUiModel.value!!.plus
        val minus = videoScoreUiModel.value!!.minus
        //_videoScoreUiModel.value = _videoScoreUiModel.value?.copy( = minus, minus = plus)

        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(leftText = minus.toString(), rightText = plus.toString())
    }

    fun leftPlusRightMinus() {
        val plus = videoScoreUiModel.value!!.plus
        val minus = videoScoreUiModel.value!!.minus

        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(leftText = plus.toString(), rightText = minus.toString())
    }

    fun changeVideo(data: Boolean) {
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(isVideoStart = data)
    }

    fun getCurrentAnalysisData(): ClickVideoListWithClickInfo? {
        return if (videoScoreUiModel.value!!.videoId.isNotEmpty()) {
            ClickVideoListWithClickInfo(
                videoScoreUiModel.value!!.videoInfo!!,
                videoScoreUiModel.value!!.startPoint.toInt(),
                videoScoreUiModel.value!!.videoId,
                videoScoreUiModel.value!!.plus,
                videoScoreUiModel.value!!.minus,
                videoScoreUiModel.value!!.total,
                videoScoreUiModel.value!!.clickInfoList,
            )
        } else {
            null
        }
    }

    fun insertVideoData(success: () -> Unit, failed: () -> Unit) {
        val analysisData = getCurrentAnalysisData()
        if (analysisData != null) {
            viewModelScope.launch {
                clickVideoRepository.insert(analysisData)
                writeDataFile(settingUiModel.value!!.externalFileDate)
            }
            success()
        } else {
            failed()
        }
    }

    fun clearRankingData() {
        val updateData = _ranking.value!!

        updateData.clear()
        _ranking.value = updateData
    }

    private fun getSettingData() {
        val mutex = Mutex()

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getIsChangeButton().collect { isChange ->
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        if (_settingUiModel.value?.isChangeButton != isChange) {
                            _settingUiModel.value =
                                _settingUiModel.value!!.copy(isChangeButton = isChange)
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getIsVibButton().collect { isVib ->
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        if (_settingUiModel.value?.isVidButton != isVib) {
                            _settingUiModel.value =
                                _settingUiModel.value!!.copy(isVidButton = isVib)
                        }
                    }
                }
            }
        }


        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getMode().collect() {
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        if (settingUiModel.value?.mode != intToMode.get(it)!!) {
                            _settingUiModel.postValue(
                                _settingUiModel.value?.copy(
                                    mode = intToMode.get(
                                        it
                                    )!!
                                )
                            )
                            Log.d(TAG, "getSettingData: ${_settingUiModel.value} 3")
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getSetStartPoint().collect(){
                Log.d(TAG, "getSettingData: ${it} 3")
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        if (settingUiModel.value?.setStartPoint != it) {
                            _settingUiModel.postValue(
                                _settingUiModel.value?.copy(
                                    setStartPoint = it
                                )
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getSetStartPoint().collect(){
                Log.d(TAG, "getSettingData: ${it} 3")
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        if (settingUiModel.value?.setStartPoint != it) {
                            _settingUiModel.postValue(
                                _settingUiModel.value?.copy(
                                    setStartPoint = it
                                )
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val date = settingRepository.getExternalFileDate().first()

            if(date.isNullOrBlank()){
                saveExternalFileDate()
            }
            else{
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        _settingUiModel.postValue(
                            _settingUiModel.value?.copy(
                                externalFileDate = date
                            )
                        )
                    }
                }
            }
        }
    }
}