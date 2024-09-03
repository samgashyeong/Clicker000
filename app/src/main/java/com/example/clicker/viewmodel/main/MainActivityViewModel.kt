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
import kotlinx.coroutines.launch
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
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getIsChangeButton().collect(){
                Log.d(ContentValues.TAG, "getDataIsChange : ${it}")
                if(settingUiModel.value?.isChangeButton != it){
                    _settingUiModel.value = _settingUiModel.value?.copy(isChangeButton = it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getIsVibButton().collect(){
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                if(settingUiModel.value?.isVidButton != it){
                    _settingUiModel.value = _settingUiModel.value?.copy(isVidButton = it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getMode().collect(){
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                if(settingUiModel.value?.mode != intToMode.get(it)!!){
                    _settingUiModel.value = _settingUiModel.value?.copy(mode = intToMode.get(it)!!)
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