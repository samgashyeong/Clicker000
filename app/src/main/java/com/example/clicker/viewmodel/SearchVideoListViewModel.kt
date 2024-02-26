package com.example.clicker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clicker.data.database.ClickVideoListWithClickInfo

class SearchVideoListViewModel : ViewModel() {
    var databaseScoredList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()
    var searchList : MutableLiveData<List<ClickVideoListWithClickInfo>?> = MutableLiveData()


//    private fun databaseListToTitle() = searchList.value?.map {
//        it.videoInfo.snippet.title
//    }
    fun findVideo(searchText : String){

        searchList.value = databaseScoredList.value?.filter {
            it.videoInfo.snippet.title.uppercase().contains(searchText.uppercase())
        }
    }

    fun getAllVideos(value: List<ClickVideoListWithClickInfo>?) {
        searchList.value = value
    }
}