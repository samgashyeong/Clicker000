package com.example.clicker

import androidx.databinding.BindingAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


object BindingAdapter {
    @JvmStatic
    @BindingAdapter("setVideoId", "setStartPoint")
    fun setVideoId(view: YouTubePlayerView?, videoId: String, startPoint : Float) {
        view?.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, startPoint)
            }
        })
    }
}