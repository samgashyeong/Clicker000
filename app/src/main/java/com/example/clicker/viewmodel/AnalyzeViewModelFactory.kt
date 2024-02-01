package com.example.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo

class AnalyzeViewModelFactory(private val data : ClickVideoListWithClickInfo,
    private val clickInfo : List<ClickInfo>,
    private val videoId : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalyzeViewModel(MutableLiveData(data), MutableLiveData(clickInfo), MutableLiveData(videoId)) as T

    }
}