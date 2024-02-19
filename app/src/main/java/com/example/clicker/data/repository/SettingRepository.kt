package com.example.clicker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicker.data.database.Setting
import com.example.clicker.data.database.SettingDataStoreKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("datastore")
class SettingRepository(private val context: Context) {


    suspend fun getSetting() : Flow<Setting?> = context.dataStore.data.map { it->
        it[SettingDataStoreKey.IS_CHANGE_BUTTON]?.let { it1 -> it[SettingDataStoreKey.IS_VIBRATE]?.let { it2 ->
            Setting(it1,
                it2
            )
        } }
    }

    suspend fun saveSWITCH(setting : Setting){
        context.dataStore.edit { it->
            it[SettingDataStoreKey.IS_CHANGE_BUTTON] = setting.isChangeButton
            it[SettingDataStoreKey.IS_VIBRATE] = setting.isVarivarte
        }
    }
}