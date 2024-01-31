package com.example.clicker.data.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.database.room.ClickVideoDao
import com.example.clicker.data.database.room.ClickVideoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ClickVideoRepository(private val clickVideoDao: ClickVideoDao) {
    val readAll = clickVideoDao.getAll()

    suspend fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        try {
            GlobalScope.launch(Dispatchers.IO) {
                clickVideoDao.insert(clickVideoListWithClickInfo)
            }
        }catch (e: Exception){}
    }
}