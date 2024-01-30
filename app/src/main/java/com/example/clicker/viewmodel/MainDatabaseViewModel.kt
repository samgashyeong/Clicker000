package com.example.clicker.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainDatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val clickVideoRepository : ClickVideoRepository = ClickVideoRepository(application)
    var clickVideos : MutableLiveData<List<ClickVideoListWithClickInfo>>? = null
    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            clickVideos!!.value = clickVideoRepository.getAll()
            Log.d("ContentValues", "getAll: ${clickVideos!!.value?.get(0)!!.videoInfo.snippet.title}")
        }
    }

    fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        viewModelScope.launch(Dispatchers.IO) {
            clickVideoRepository.insert(clickVideoListWithClickInfo)
        }
    }
}
