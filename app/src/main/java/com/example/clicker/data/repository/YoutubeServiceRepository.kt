package com.example.clicker.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.clicker.data.remote.api.youtubeservice.YoutubeService
import com.example.clicker.data.remote.api.youtubeservice.YoutubeServiceRetrofit
import com.example.clicker.data.remote.model.youtube.Item
import com.example.clicker.data.remote.model.youtube.VideoList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YoutubeServiceRepository {
    private var youtubeService
    = YoutubeServiceRetrofit.retrofit.create(YoutubeService::class.java)

    fun searchYoutubeInfo(part : String, id : String, key : String) : Item{
        var videoItem : Item? = null
        youtubeService.getVideoProfile(part, id, key).enqueue(object : Callback<VideoList> {
            override fun onResponse(call: Call<VideoList>, response: Response<VideoList>) {
                if(response.code() == 200){
                    Log.d(TAG, "onResponse: ${response.body()!!.items[0].snippet.title}")
                    videoItem = response.body()!!.items[0]
                }
            }
            override fun onFailure(call: Call<VideoList>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
        return videoItem!!
    }
}