package com.example.clicker.data.database.room

import androidx.room.TypeConverter
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.remote.model.youtube.Item
import com.google.gson.Gson

class ClickInfoTypeConverter() {
    @TypeConverter
    fun jsonToList(value: String): List<ClickInfo>? {
        return Gson().fromJson(value, Array<ClickInfo>::class.java).toList()
    }

    @TypeConverter
    fun fromList(value: List<ClickInfo>): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToObject(value: String): Item? {
        return Gson().fromJson(value, Item::class.java)
    }

    @TypeConverter
    fun fromObject(value: Item): String{
        return Gson().toJson(value)
    }
}