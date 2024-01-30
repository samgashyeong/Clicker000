package com.example.clicker.data.database.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.clicker.data.database.ClickVideoListWithClickInfo


@Dao
interface ClickVideoDao {

    @Transaction
    @Query("SELECT * FROM ClickVideoInfo") // 오름차순 : ACS 내림차순 : DESC
    fun getAll(): List<ClickVideoListWithClickInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: ClickVideoListWithClickInfo)

}