package com.example.clicker.model.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object YoutubeServiceRetrofit {
    private const val BASE_URL = "https://youtube.googleapis.com/youtube/v3/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}