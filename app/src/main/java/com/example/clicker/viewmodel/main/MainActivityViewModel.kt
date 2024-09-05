package com.example.clicker.viewmodel.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.SettingRepository
import com.example.clicker.data.repository.YoutubeServiceRepository
import com.example.clicker.util.VibrationProvider
import com.example.clicker.util.intToMode
import com.example.clicker.util.modeToInt
import com.example.clicker.viewmodel.Mode
import com.example.clicker.viewmodel.main.model.SettingUiModel
import com.example.clicker.viewmodel.main.model.VideoScoreUiModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val tracker: YouTubePlayerTracker,
    private val vibrationProvider: VibrationProvider,
    private val clickVideoRepository: ClickVideoRepository,
    private val settingRepository: SettingRepository,
    private val youtubeServiceRepository: YoutubeServiceRepository,
) : ViewModel() {
    private val _videoScoreUiModel: MutableLiveData<VideoScoreUiModel> =
        MutableLiveData(VideoScoreUiModel(videoInfo = null))
    val videoScoreUiModel: LiveData<VideoScoreUiModel> get() = _videoScoreUiModel

    private val _settingUiModel: MutableLiveData<SettingUiModel> = MutableLiveData(SettingUiModel())
    val settingUiModel: LiveData<SettingUiModel> get() = _settingUiModel

    init {
        getSettingData()
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

    fun saveMode(mode: Mode) {
        Log.d(TAG, "saveMode: ${mode.toString()}")
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveMode(modeToInt[mode]!!)
        }
    }

    fun changeStartPoint(startPoint: Float) {
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(startPoint = startPoint)
    }

    private fun plus() {
        val value = videoScoreUiModel.value!!.plus
        val totalValue = videoScoreUiModel.value!!.total
        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(plus = value + 1, total = totalValue + 1)

        val updateList = videoScoreUiModel.value?.clickInfoList
        updateList!!.add(
            ClickInfo(
                clickSecond = tracker.currentSecond,
                clickScorePoint = +1,
                null,
                videoScoreUiModel.value!!.minus,
                videoScoreUiModel.value!!.plus,
                videoScoreUiModel.value!!.total
            )
        )
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(clickInfoList = updateList)
    }

    private fun minus() {
        val totalValue = videoScoreUiModel.value!!.total
        val value = videoScoreUiModel.value!!.minus
        _videoScoreUiModel.value =
            _videoScoreUiModel.value?.copy(minus = value - 1, total = totalValue-1)
        val updateList = videoScoreUiModel.value?.clickInfoList
        updateList!!.add(
            ClickInfo(
                clickSecond = tracker.currentSecond,
                clickScorePoint = -1,
                null,
                videoScoreUiModel.value!!.minus,
                videoScoreUiModel.value!!.plus,
                videoScoreUiModel.value!!.total
            )
        )
        _videoScoreUiModel.value = _videoScoreUiModel.value!!.copy(clickInfoList = updateList)
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

    fun getVideoInfo(id: String, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoInfo = youtubeServiceRepository.searchYoutubeInfo("snippet", id, key)
            _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(videoInfo = videoInfo)
        }
    }


    fun leftMinusRightPlus(){
        val plus = videoScoreUiModel.value!!.plus
        val minus = videoScoreUiModel.value!!.minus
        //_videoScoreUiModel.value = _videoScoreUiModel.value?.copy( = minus, minus = plus)

        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(leftText = minus.toString(), rightText = plus.toString())
    }

    fun leftPlusRightMinus(){
        val plus = videoScoreUiModel.value!!.plus
        val minus = videoScoreUiModel.value!!.minus

        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(leftText = plus.toString(), rightText = minus.toString())
    }
}