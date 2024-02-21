package com.example.clicker.data.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.database.room.ClickVideoDao
import com.example.clicker.data.database.room.ClickVideoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Flow
import kotlin.coroutines.resume

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