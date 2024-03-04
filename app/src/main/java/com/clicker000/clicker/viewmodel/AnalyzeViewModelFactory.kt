package com.clicker000.clicker.viewmodel

import androidx.lifecycle.ViewModelProvider
import com.clicker000.clicker.data.database.ClickInfo
import com.clicker000.clicker.data.database.ClickVideoListWithClickInfo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class AnalyzeViewModelFactory(private val data : ClickVideoListWithClickInfo,
    private val clickInfo : List<ClickInfo>,
    private val videoId : String,
    private val tracker: YouTubePlayerTracker,
    private val nowPosition: Int) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return AnalyzeViewModel(MutableLiveData(data), MutableLiveData(clickInfo), MutableLiveData(videoId), tracker, MutableLiveData(nowPosition)) as T
//
//    }
}