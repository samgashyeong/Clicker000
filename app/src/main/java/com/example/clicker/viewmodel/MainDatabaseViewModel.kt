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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.Serializable

class MainDatabaseViewModel(application: Application) : AndroidViewModel(application), Serializable {
    val readAllData:MutableLiveData<List<ClickVideoListWithClickInfo>> = MutableLiveData();
    private val repository:ClickVideoRepository

    init {
        val userDao = ClickVideoDatabase.getInstance(application)!!.clickVideoDao()
        repository = ClickVideoRepository(userDao)
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAll().collect{
                readAllData.postValue(it)
            }
        }
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
