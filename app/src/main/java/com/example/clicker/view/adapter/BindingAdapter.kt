package com.example.clicker.view.adapter

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.media.Image
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


object BindingAdapter {
//    @JvmStatic
//    @BindingAdapter("video_id", "start_point")
//    fun setVideoId(view: YouTubePlayerView?, videoId: String?, startPoint : Float?) {
//        if(startPoint !=  null && videoId != null){
//            view?.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
//                override fun onReady(youTubePlayer: YouTubePlayer) {
//                    Log.d(TAG, "onReady: ")
//                    youTubePlayer.loadVideo(videoId, startPoint)
//                }
//            })
//        }
//    }

    @JvmStatic
    @BindingAdapter("app:link")
    fun setImage(imageView: ImageView, url : String){
        Glide.with(imageView.context).load(url).into(imageView)
    }
}