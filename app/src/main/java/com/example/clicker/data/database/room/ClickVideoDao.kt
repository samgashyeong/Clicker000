package com.example.clicker.data.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import kotlinx.coroutines.flow.Flow


@Dao
interface ClickVideoDao {

    @Transaction
    @Query("SELECT * FROM ClickVideoInfo") // 오름차순 : ACS 내림차순 : DESC
    fun getAll(): Flow<List<ClickVideoListWithClickInfo>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ClickVideoListWithClickInfo)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entity: ClickVideoListWithClickInfo)


    @Update
    suspend fun update(updateData : ClickVideoListWithClickInfo)
}