package com.clicker000.clicker.data.remote.api.youtubeservice

import com.clicker000.clicker.data.remote.model.youtube.VideoList
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