package com.example.clicker.data.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.clicker.data.database.ClickVideoListWithClickInfo


@Database(entities = [ClickVideoListWithClickInfo::class], version = 9)
@TypeConverters(ClickInfoTypeConverter::class)
abstract class ClickVideoDatabase : RoomDatabase() {

    abstract fun clickVideoDao() : ClickVideoDao
//    companion object {
//        private var INSTANCE: ClickVideoDatabase? = null
//
//        fun getInstance(context: Context): ClickVideoDatabase? {
//            if (INSTANCE == null) {
//                synchronized(ClickVideoDatabase::class) {
//                    INSTANCE = Room.databaseBuilder(context.applicationContext,
//                        ClickVideoDatabase::class.java, "click_video.db")
//                        .fallbackToDestructiveMigration()
//                        .build()
//                }
//            }
//            return INSTANCE
//        }
//    }
}