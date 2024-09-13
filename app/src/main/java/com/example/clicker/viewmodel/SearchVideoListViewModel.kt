package com.example.clicker.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.ExternalStorageRepository
import com.example.clicker.data.repository.SettingRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchVideoListViewModel @Inject constructor(
    private val databaseVideoRepository: ClickVideoRepository,
    private val externalStorageRepository: ExternalStorageRepository,
    private val settingRepository: SettingRepository
): ViewModel() {
    private val _databaseScoredList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()
    val databaseScoredList : LiveData<List<ClickVideoListWithClickInfo>?> get() = _databaseScoredList

    private val _searchList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()
    val searchList : LiveData<List<ClickVideoListWithClickInfo>?> get() = _searchList

    init {
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseVideoRepository.getAll().collect{
                if(!it.equals(databaseScoredList)){
                    val dataToString = Gson().toJson(it)
                    saveFile(dataToString)
                    withContext(Dispatchers.Main){
                        _databaseScoredList.value = it
                        _searchList.value = it
                    }
                }
            }
        }
    }

    private fun saveFile(dataToString: String) {
        externalStorageRepository.findClickFile(dataToString, "")
    }

    fun findVideo(searchText : String){

        Log.d(TAG, "findVideo: ${searchText.uppercase()}")
        _searchList.value = databaseScoredList.value?.filter {
            it.videoInfo.snippet.title.uppercase().contains(searchText.uppercase())
        }
    }

    fun insertAll(list : List<ClickVideoListWithClickInfo>){
        Log.d(TAG, "insertAll: ${list}")
        viewModelScope.launch {
            databaseVideoRepository.insertAll(list)
        }
    }
    fun getAllVideos() {
        _searchList.value = databaseScoredList.value
    }
}