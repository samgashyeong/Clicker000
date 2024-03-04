package com.clicker000.clicker.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.clicker000.clicker.data.remote.api.youtubeservice.YoutubeService
import com.clicker000.clicker.data.remote.model.youtube.Item
import com.clicker000.clicker.data.remote.model.youtube.VideoList
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume

class YoutubeServiceRepository(private val youtubeService: YoutubeService) {
//    private var youtubeService
//    = YoutubeServiceRetrofit.retrofit.create(YoutubeService::class.java)

    suspend fun searchYoutubeInfo(part : String, id : String, key : String) : Item{
        val response = suspendCancellableCoroutine{ cancellableContinuation ->
            youtubeService.getVideoProfile(part, id, key).enqueue(object : Callback<VideoList> {
                override fun onResponse(call: Call<VideoList>, response: Response<VideoList>) {
                    if(response.code() == 200){
                        //Log.d(TAG, "onResponse: ${response.body()!!.items[0].snippet.title}")
                        cancellableContinuation.resume(response)
                    }
                }
                override fun onFailure(call: Call<VideoList>, t: Throwable) {
                }

            })
        }

        Log.d(TAG, "searchYoutubeInfo: ${response.body()!!.items[0].snippet.title}")
        return response.body()!!.items[0]!!
    }
}