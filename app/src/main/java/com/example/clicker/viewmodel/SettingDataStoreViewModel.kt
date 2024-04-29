package com.example.clicker.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingDataStoreViewModel @Inject constructor(private val dataRepo : SettingRepository) : ViewModel(){
    //var isSwitchOn : MutableLiveData<Setting?> = MutableLiveData()
    var isChangeButton : MutableLiveData<Boolean?> = MutableLiveData()
    var isVibButton : MutableLiveData<Boolean?> = MutableLiveData()

    init {
        getIsChangeButton()
        getIsvibButton()
    }

    private fun getIsChangeButton(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getIsChangeButton().collect(){
                Log.d(ContentValues.TAG, "getDataIsChange : ${it}")
                if(isChangeButton.value != it){
                    isChangeButton.postValue(it)
                }
            }
        }
    }

    private fun getIsvibButton(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getIsVibButton().collect(){
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                if(isVibButton.value != it){
                    isVibButton.postValue(it)
                }
            }
        }
    }
//    private fun getData(){
//        viewModelScope.launch(Dispatchers.IO) {
//            dataRepo.getSetting().collect(){
//                Log.d(ContentValues.TAG, "getData: ${it}")
//                isSwitchOn.postValue(it)
//            }
//        }
//    }

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