package com.example.clicker.data.database

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingDataStoreKey {
    val IS_CHANGE_BUTTON = booleanPreferencesKey("is_change_button")
    val IS_VIBRATE = booleanPreferencesKey("is_vibrate")
    val IS_FIRST_START = booleanPreferencesKey("is_first_start")
    val MODE = intPreferencesKey("mode")
    val SET_START_POINT = booleanPreferencesKey("set_start_point")
    val EXTERNAL_STORAGE_DATE = stringPreferencesKey("externalStorageDate")
}