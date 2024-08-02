package com.example.clicker.viewmodel

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicker.data.repository.SettingRepository
import com.example.clicker.util.intToMode
import com.example.clicker.util.modeToInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Mode(){
    data class Default(val name : String = "Default") : Mode()
    data class Ranking(val name : String = "Ranking") : Mode()
}
@HiltViewModel
class SettingDataStoreViewModel @Inject constructor(private val dataRepo : SettingRepository) : ViewModel(){
    //var isSwitchOn : MutableLiveData<Setting?> = MutableLiveData()
    var isChangeButton : MutableLiveData<Boolean?> = MutableLiveData()
    var isVibButton : MutableLiveData<Boolean?> = MutableLiveData()
    var mode : MutableLiveData<Mode> = MutableLiveData()

    init {
        getIsChangeButton()
        getIsvibButton()
        getMode()
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

    private fun getMode(){
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.getMode().collect(){
                Log.d(ContentValues.TAG, "getDataVibData : ${it}")
                if(mode.value != intToMode.get(it)!!){
                    mode.postValue(intToMode.get(it)!!)
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
    fun saveMode(mode: Mode){
        Log.d(TAG, "saveMode: ${mode.toString()}")
        viewModelScope.launch(Dispatchers.IO) {
            dataRepo.saveMode(modeToInt[mode]!!)
        }
    }
}