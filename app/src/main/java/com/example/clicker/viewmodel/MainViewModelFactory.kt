package com.example.clicker.viewmodel

import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class MainViewModelFactory(private val traker : YouTubePlayerTracker): ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return MainViewModel(
//            tracker = traker,
//            MutableLiveData("")) as T
//
//    }
}