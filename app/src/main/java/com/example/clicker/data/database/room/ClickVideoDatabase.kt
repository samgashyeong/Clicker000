package com.example.clicker.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.clicker.data.database.ClickVideoListWithClickInfo


@Database(entities = [ClickVideoListWithClickInfo::class], version = 4)
abstract class ClickVideoDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: ClickVideoDatabase? = null

        fun getInstance(context: Context): ClickVideoDatabase? {
            if (INSTANCE == null) {
                synchronized(ClickVideoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ClickVideoDatabase::class.java, "click_video")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}