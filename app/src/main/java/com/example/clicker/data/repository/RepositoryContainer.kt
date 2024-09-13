package com.example.clicker.data.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.clicker.data.database.room.ClickVideoDao
import com.example.clicker.data.database.room.ClickVideoDatabase
import com.example.clicker.data.remote.api.youtubeservice.YoutubeService
import com.example.clicker.util.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryContainer {

    @Singleton
    @Provides
    fun providesDatabaseStoreViewModel(application: Application) = SettingRepository(application)


    @Singleton
    @Provides
    fun providesYoutubeApi() = Retrofit.Builder()
    .baseUrl(Utils.youtubeUrl)
    .addConverterFactory(GsonConverterFactory.create())
    .build().create(YoutubeService::class.java)


    @Provides
    fun providesYoutubeRepo() = YoutubeServiceRepository(RepositoryContainer.providesYoutubeApi())

    @Provides
    fun providesDatabaseViewModel(application: Application) = ClickVideoRepository(
        provideNoteDAO(
            provideNoteDatabase(application)
        )
    )

    @Singleton
    @Provides
    fun provideNoteDatabase(@ActivityContext context: Context) : ClickVideoDatabase {
        return Room.databaseBuilder(context.applicationContext,
                        ClickVideoDatabase::class.java, "click_video.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }

    @Singleton
    @Provides
    fun provideExternalStorage(@ApplicationContext context: Context, repository: SettingRepository) = ExternalStorageRepository(context, repository)

    @Provides
    fun provideNoteDAO(clickVideo: ClickVideoDatabase): ClickVideoDao {
        return clickVideo.clickVideoDao()
    }
}