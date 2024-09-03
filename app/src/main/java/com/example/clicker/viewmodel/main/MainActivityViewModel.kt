package com.example.clicker.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.SettingRepository
import com.example.clicker.data.repository.YoutubeServiceRepository
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