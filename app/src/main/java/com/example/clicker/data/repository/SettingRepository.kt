package com.example.clicker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicker.data.database.SettingDataStoreKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("datastore")
class SettingRepository(private val context: Context) {

    fun getSetStartPoint() : Flow<Boolean> = context.dataStore.data.map {
        return@map it[SettingDataStoreKey.IS_CHANGE_BUTTON] ?: false
    }

    suspend fun getIsChangeButton() : Flow<Boolean> = context.dataStore.data.map { it->
        return@map it[SettingDataStoreKey.IS_CHANGE_BUTTON] ?: false

    }

    suspend fun getIsVibButton() : Flow<Boolean> = context.dataStore.data.map { it->
        return@map it[SettingDataStoreKey.IS_VIBRATE] ?: false
    }

    suspend fun getMode() : Flow<Int> = context.dataStore.data.map {
        return@map it[SettingDataStoreKey.MODE] ?: 0
    }

    suspend fun saveIsChangeButton(setting : Boolean){
        context.dataStore.edit { it->
            it[SettingDataStoreKey.IS_CHANGE_BUTTON] = setting
        }
    }

    suspend fun saveSetStartPoint(setting : Boolean){
        context.dataStore.edit {
            it[SettingDataStoreKey.SET_START_POINT] = setting
        }
    }
    suspend fun saveIsVibButton(setting : Boolean){
        context.dataStore.edit { it->
            it[SettingDataStoreKey.IS_VIBRATE] = setting
        }
    }

    suspend fun saveMode(setting : Int){
        context.dataStore.edit { it->
            it[SettingDataStoreKey.MODE] = setting
        }
    }
}