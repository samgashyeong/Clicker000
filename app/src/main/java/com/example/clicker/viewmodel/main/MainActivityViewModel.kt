package com.example.clicker.viewmodel.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.SettingRepository
import com.example.clicker.data.repository.YoutubeServiceRepository
import com.example.clicker.util.intToMode
import com.example.clicker.util.modeToInt
import com.example.clicker.viewmodel.Mode
import com.example.clicker.viewmodel.main.model.SettingUiModel
import com.example.clicker.viewmodel.main.model.VideoScoreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val clickVideoRepository: ClickVideoRepository,
    private val settingRepository : SettingRepository,
    private val youtubeServiceRepository: YoutubeServiceRepository
) : ViewModel() {
    private val _videoScoreUiModel : MutableLiveData<VideoScoreUiModel> = MutableLiveData(VideoScoreUiModel(videoInfo = null))
    val videoScoreUiModel : LiveData<VideoScoreUiModel> get() = _videoScoreUiModel

    private val _settingUiModel : MutableLiveData<SettingUiModel> = MutableLiveData(SettingUiModel())
    val settingUiModel : LiveData<SettingUiModel> get() = _settingUiModel

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
                            _settingUiModel.value = _settingUiModel.value!!.copy(isChangeButton = isChange)
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
                            _settingUiModel.value = _settingUiModel.value!!.copy(isVidButton = isVib)
                        }
                    }
                }
            }
        }


        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getMode().collect() {
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                mutex.withLock {
                    withContext(Dispatchers.Main){
                        if (settingUiModel.value?.mode != intToMode.get(it)!!) {
                            _settingUiModel.postValue(_settingUiModel.value?.copy(mode = intToMode.get(it)!!))
                            Log.d(TAG, "getSettingData: ${_settingUiModel.value} 3")
                        }
                    }
                }
            }
        }
    }




    fun saveIsChangeButton(isSwitchOn: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveIsChangeButton(isSwitchOn)
        }
    }
    fun saveIsVibButton(isSwitchOn: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveIsVibButton(isSwitchOn)
        }
    }
    fun saveMode(mode: Mode){
        Log.d(TAG, "saveMode: ${mode.toString()}")
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.saveMode(modeToInt[mode]!!)
        }
    }

    fun changeStartPoint(startPoint : Float){
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(startPoint = startPoint)
    }

    private fun plus(){
        val value = videoScoreUiModel.value!!.plus
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(plus = value+1, total = value+1)
    }

    private fun minus(minus : Int){
        val value = videoScoreUiModel.value!!.minus
        _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(minus = value-1, total = value-1)
    }

    fun rightButton(isChangeButton : Boolean){

    }

    fun getVideoInfo(id : String, key : String){
        viewModelScope.launch(Dispatchers.IO) {
            val videoInfo = youtubeServiceRepository.searchYoutubeInfo("snippet", id, key)
            _videoScoreUiModel.value = _videoScoreUiModel.value?.copy(videoInfo = videoInfo)
        }
    }


}