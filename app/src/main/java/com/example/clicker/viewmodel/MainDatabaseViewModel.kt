package com.example.clicker.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.nfc.Tag
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.database.room.ClickVideoDatabase
import com.example.clicker.data.repository.ClickVideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class MainDatabaseViewModel(application: Application) : AndroidViewModel(application), Serializable {
    val readAllData:LiveData<List<ClickVideoListWithClickInfo>>
    private val repository:ClickVideoRepository

    init {
        val userDao = ClickVideoDatabase.getInstance(application)!!.clickVideoDao()
        repository = ClickVideoRepository(userDao)
        readAllData = repository.readAll
    }

    fun getAll() : List<ClickVideoListWithClickInfo>? {
        return readAllData.value
    }

    fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(clickVideoListWithClickInfo)
        }
    }

    fun update(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(clickVideoListWithClickInfo)
        }
    }
}
