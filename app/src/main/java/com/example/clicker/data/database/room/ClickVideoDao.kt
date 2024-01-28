package com.example.clicker.data.database.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.clicker.data.database.ClickVideoListWithClickInfo

@Dao
interface ClickVideoDao {
    @Query("SELECT * FROM clickVideoList") // 오름차순 : ACS 내림차순 : DESC
    fun getAll(): LiveData<List<ClickVideoListWithClickInfo>>
}