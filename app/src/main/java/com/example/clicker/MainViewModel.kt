package com.example.clicker

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class MainViewModel(val urlStringF : MutableLiveData<String>,
                    val plusF : MutableLiveData<Int>,
                    val minusF : MutableLiveData<Int>,
                    val totalF : MutableLiveData<Int>) : ViewModel() {

    val urlString : MutableLiveData<String> = urlStringF
    val plus : MutableLiveData<Int> = plusF
    val minus : MutableLiveData<Int> = minusF
    val total : MutableLiveData<Int> = totalF



    fun extractYouTubeVideoId(url: String): MutableLiveData<String> {
        val basePart = url.substringAfterLast( "v=")
        Log.d(TAG, "extractYouTubeVideoId: ${basePart}")
        Log.d(TAG, "extractYouTubeVideoId: ${basePart.substringBefore("&si=")}")
        return MutableLiveData(basePart.substringBefore("&si="))
    }

}