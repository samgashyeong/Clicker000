package com.example.clicker.viewmodel

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object ViewModelContainer {
    @Provides
    fun provideYoutubeTrakcer() = YouTubePlayerTracker()
}