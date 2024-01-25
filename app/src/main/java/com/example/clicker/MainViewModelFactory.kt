package com.example.clicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(
    val urlString: String,
    val plus: Int,
    val minus: Int,
    val total: Int
    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(MutableLiveData(urlString), MutableLiveData(0), MutableLiveData(0), MutableLiveData(0)) as T
    }
}