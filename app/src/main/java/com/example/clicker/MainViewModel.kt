package com.example.clicker

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class MainViewModel(val urlString : MutableLiveData<String>,
                    val plus : MutableLiveData<Int>,
                    val minus : MutableLiveData<Int>,
                    val total : MutableLiveData<Int>,
    val startPoint : MutableLiveData<Float>) : ViewModel() {



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