package com.example.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.data.database.ClickInfo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class MainViewModelFactory(private val traker : YouTubePlayerTracker): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(MutableLiveData(""),
            MutableLiveData(0),
            MutableLiveData(0),
            MutableLiveData(0),
            MutableLiveData(null),
            MutableLiveData(ArrayList<ClickInfo>()),
            tracker = traker,
            MutableLiveData("")) as T

    }
}