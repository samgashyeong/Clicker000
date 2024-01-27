package com.example.clicker.model.network

import android.telecom.Call
import com.example.clicker.model.data.youtube.VideoList
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeService {
    @GET("/youtube/v3/videos")
    fun getVideoProfile(
        @Query("part") part: String,
        @Query("id") id: String,
        @Query("key") key: String
    ) : retrofit2.Call<VideoList>
}