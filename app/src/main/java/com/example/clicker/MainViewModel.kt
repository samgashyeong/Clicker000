package com.example.clicker

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(val urlStringF : MutableLiveData<String>,
                    val plusF : MutableLiveData<Int>,
                    val minusF : MutableLiveData<Int>,
                    val totalF : MutableLiveData<Int>) : ViewModel() {

    val urlString : MutableLiveData<String> = urlStringF
    val plus : MutableLiveData<Int> = plusF
    val minus : MutableLiveData<Int> = minusF
    val total : MutableLiveData<Int> = totalF


//    fun returnYoutubeID(url : String) : MutableLiveData<String>{
//
//    }

}