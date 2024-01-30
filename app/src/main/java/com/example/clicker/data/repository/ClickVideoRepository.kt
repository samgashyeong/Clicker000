package com.example.clicker.data.repository

import android.app.Application
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.data.database.room.ClickVideoDao
import com.example.clicker.data.database.room.ClickVideoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClickVideoRepository(application: Application) {
    private lateinit var clickVideoDatabase : ClickVideoDatabase
    private lateinit var clickVideoDao : ClickVideoDao
    private lateinit var clickVideos : List<ClickVideoListWithClickInfo>

    init {
        GlobalScope.launch(Dispatchers.IO) {
            clickVideoDatabase = ClickVideoDatabase.getInstance(application)!!
            clickVideoDao = clickVideoDatabase.clickVideoDao()
            clickVideos = clickVideoDao.getAll()
        }
    }
    fun getAll(): List<ClickVideoListWithClickInfo> {
        return clickVideos
    }

    fun insert(clickVideoListWithClickInfo: ClickVideoListWithClickInfo){
        try {
            GlobalScope.launch(Dispatchers.IO) {
                clickVideoDao.insert(clickVideoListWithClickInfo)
            }
        }catch (e: Exception){}
    }
}