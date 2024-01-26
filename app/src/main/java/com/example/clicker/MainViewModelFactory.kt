package com.example.clicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(MutableLiveData(""), MutableLiveData(0), MutableLiveData(0), MutableLiveData(0), MutableLiveData(0f)) as T
    }
}