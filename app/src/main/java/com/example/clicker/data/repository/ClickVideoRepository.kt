package com.example.clicker.data.repository

import android.app.Application
import com.example.clicker.data.database.room.ClickVideoDatabase

class ClickVideoRepository(application: Application) {
    private val clickVideoDatabase : ClickVideoDatabase = ClickVideoDatabase.getInstance(application)!!
}