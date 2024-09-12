package com.example.clicker.data.repository

import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.database.room.ClickVideoDao

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

    suspend fun insertAll(clickVideoListWithClickInfo: List<ClickVideoListWithClickInfo>){
        clickVideoDao.insertAll(*clickVideoListWithClickInfo.toTypedArray())
    }


}