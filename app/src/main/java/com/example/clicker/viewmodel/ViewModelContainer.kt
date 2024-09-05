package com.example.clicker.viewmodel

import com.example.clicker.util.VibrationProvider
import com.example.clicker.util.VibrationProviderImpl
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object ViewModelContainer {
    @Provides
    fun provideYoutubeTrakcer() = YouTubePlayerTracker()

}

@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelCon{
    @Binds
    abstract fun bindVibrator(quc : VibrationProviderImpl) : VibrationProvider
}