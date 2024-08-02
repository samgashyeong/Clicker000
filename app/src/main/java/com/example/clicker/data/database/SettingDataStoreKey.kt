package com.example.clicker.data.database

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object SettingDataStoreKey {
    val IS_CHANGE_BUTTON = booleanPreferencesKey("is_change_button")
    val IS_VIBRATE = booleanPreferencesKey("is_vibrate")
    val IS_FIRST_START = booleanPreferencesKey("is_first_start")
    val MODE = intPreferencesKey("mode")
}