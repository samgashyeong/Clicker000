package com.clicker000.clicker.data.repository

import com.clicker000.clicker.data.database.ClickVideoListWithClickInfo
import com.clicker000.clicker.data.database.room.ClickVideoDao

class ClickVideoRepository(private val clickVideoDao: ClickVideoDao) {
    //val readAll = clickVideoDao.getAll()

    fun getAll() : kotlinx.coroutines.flow.Flow<List<ClickVideoListWithClickInfo>>{
        return clickVideoDao.getAll()
    }
    suspend fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        clickVideoDao.insert(clickVideoListWithClickInfo)
    }

    suspend fun update(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        clickVideoDao.update(clickVideoListWithClickInfo)
    }


}