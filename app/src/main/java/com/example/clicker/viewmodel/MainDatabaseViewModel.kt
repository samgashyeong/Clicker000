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
        repository = ClickVideoRepository(userDao) //이니셜라이즈 해줍니다.
        readAllData = repository.readAll // readAlldata는 repository에서 만들어줬던 livedata입니다.
    }

    fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(clickVideoListWithClickInfo)
        }
    }
}
