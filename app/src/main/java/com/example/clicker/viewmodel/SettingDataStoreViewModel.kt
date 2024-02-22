package com.example.clicker.viewmodel

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.database.Setting
import com.example.clicker.data.repository.SettingRepository
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingDataStoreViewModel @Inject constructor(private val dataRepo : SettingRepository) : ViewModel(){
    var isSwitchOn : MutableLiveData<Setting?> = MutableLiveData()
    var isChagneButton : MutableLiveData<Boolean?> = MutableLiveData()
    var isVibButton : MutableLiveData<Boolean?> = MutableLiveData()

    init {

        getIsChangeButton()
        getIsvibButton()
        getData()
    }

    fun getIsChangeButton(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getIsChangeButton().collect(){
                Log.d(ContentValues.TAG, "getData: ${it}")
                isChagneButton.postValue(it)
            }
        }
    }

    fun getIsvibButton(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getIsVibButton().collect(){
                Log.d(ContentValues.TAG, "getData: ${it}")
                isVibButton.postValue(it)
            }
        }
    }
    fun getData(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getSetting().collect(){
                Log.d(ContentValues.TAG, "getData: ${it}")
                isSwitchOn.postValue(it)
            }
        }
    }

    fun saveIsChangeButton(isSwitchOn: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.saveIsChangeButton(isSwitchOn)
        }
    }
    fun saveIsvibButton(isSwitchOn: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.saveIsVibButton(isSwitchOn)
        }
    }
}