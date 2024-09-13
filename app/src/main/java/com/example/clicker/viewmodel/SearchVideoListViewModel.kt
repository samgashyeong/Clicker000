package com.example.clicker.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atwa.filepicker.result.FileMeta
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.repository.ClickVideoRepository
import com.example.clicker.data.repository.ExternalStorageRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchVideoListViewModel @Inject constructor(
    private val databaseVideoRepository: ClickVideoRepository,
    private val externalStorageRepository: ExternalStorageRepository,
): ViewModel() {
    private val _databaseScoredList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()
    val databaseScoredList : LiveData<List<ClickVideoListWithClickInfo>?> get() = _databaseScoredList

    private val _searchList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()
    val searchList : LiveData<List<ClickVideoListWithClickInfo>?> get() = _searchList

    private val _isSaveExternalFile : MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseVideoRepository.getAll().collect{
                val dataToString = Gson().toJson(it)
                Log.d(TAG, "getAll: ${dataToString} asdf")
                withContext(Dispatchers.Main){
                    _databaseScoredList.value = it
                    _searchList.value = it
                }

                if(_isSaveExternalFile.value == true){
                    saveFile(dataToString)
                    withContext(Dispatchers.Main){
                        _isSaveExternalFile.value = false
                    }
                }
            }
        }
    }

    private fun saveFile(dataToString: String) {
        viewModelScope.launch {

            externalStorageRepository.findClickFile(dataToString, "")
        }
    }

    fun findVideo(searchText : String){
        Log.d(TAG, "findVideo: ${searchText.uppercase()}")
        _searchList.value = databaseScoredList.value?.filter {
            it.videoInfo.snippet.title.uppercase().contains(searchText.uppercase())
        }
    }

    fun insertAll(list : FileMeta?){
        val listType = object : TypeToken<List<ClickVideoListWithClickInfo>>() {}.type
        val data : List<ClickVideoListWithClickInfo> = Gson().fromJson(list?.file?.readText(), listType)
        data.map { it.clickVideoListID = 0 }
        viewModelScope.launch {
            databaseVideoRepository.insertAll(
                data
            )
            _isSaveExternalFile.value = true
        }
    }
    fun getAllVideos() {
        _searchList.value = databaseScoredList.value
    }
}