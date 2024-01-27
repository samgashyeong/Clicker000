package com.example.clicker.model.network

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.clicker.model.data.youtube.Item
import com.example.clicker.model.data.youtube.VideoList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YoutubeServiceRepository {
    private var youtubeService
    = YoutubeServiceRetrofit.retrofit.create(YoutubeService::class.java)

    fun searchYoutubeId(part : String, id : String, key : String) : MutableLiveData<Item>{
        val videoItem = MutableLiveData<Item>()
        youtubeService.getVideoProfile(part, id, key).enqueue(object : Callback<VideoList> {
            override fun onResponse(call: Call<VideoList>, response: Response<VideoList>) {
                if(response.code() == 200){
                    Log.d(TAG, "onResponse: ${response.body()!!.items[0].snippet.title}")
                    videoItem.value = response.body()!!.items[0]
                }
            }
            override fun onFailure(call: Call<VideoList>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
        return videoItem
    }
}