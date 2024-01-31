package com.example.clicker.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(entities = [ClickVideoListWithClickInfo::class], version = 7)
@TypeConverters(ClickInfoTypeConverter::class)
abstract class ClickVideoDatabase : RoomDatabase() {

    abstract fun clickVideoDao() : ClickVideoDao
    companion object {
        private var INSTANCE: ClickVideoDatabase? = null

        fun getInstance(context: Context): ClickVideoDatabase? {
            if (INSTANCE == null) {
                synchronized(ClickVideoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ClickVideoDatabase::class.java, "click_video.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}