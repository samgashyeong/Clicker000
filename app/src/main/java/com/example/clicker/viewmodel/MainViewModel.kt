package com.example.clicker.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clicker.model.data.youtube.Item
import com.example.clicker.model.network.YoutubeServiceRepository
import com.example.clicker.util.Utils

class MainViewModel(val urlString : MutableLiveData<String>,
                    val plus : MutableLiveData<Int>,
                    val minus : MutableLiveData<Int>,
                    val total : MutableLiveData<Int>,
                    val startPoint : MutableLiveData<Float?>,
                    ) : ViewModel() {

    val videoInfo : MutableLiveData<Item> = MutableLiveData()
    private val youtubeServiceRepository : YoutubeServiceRepository = YoutubeServiceRepository()

    fun getVideoInfo(id : String){
        youtubeServiceRepository.searchYoutubeId("snippet", id, Utils.youtubeDataApiKey)
    }

    fun minusPoint(view : View){
        minus.value = minus.value?.plus(-1)
        total.value = total.value?.plus(-1)
    }

    fun plusPoint(view : View){
        plus.value = plus.value?.plus(1)
        total.value = total.value?.plus(1)
    }
    fun extractYouTubeVideoId(url: String): MutableLiveData<String> {
        val basePart = url.substringAfterLast( "v=")
        return MutableLiveData(basePart.substringBefore("&si="))
    }

}