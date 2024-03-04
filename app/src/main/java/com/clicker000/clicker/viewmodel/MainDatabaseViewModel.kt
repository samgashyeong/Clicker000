package com.clicker000.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clicker000.clicker.data.database.ClickVideoListWithClickInfo
import com.clicker000.clicker.data.repository.ClickVideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class MainDatabaseViewModel @Inject constructor(private val repository:ClickVideoRepository) : ViewModel(), Serializable {
    val readAllData:MutableLiveData<List<ClickVideoListWithClickInfo>> = MutableLiveData();

    init {
        //val userDao = ClickVideoDatabase.getInstance(application)!!.clickVideoDao()
        //repository = ClickVideoRepository(userDao)
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
