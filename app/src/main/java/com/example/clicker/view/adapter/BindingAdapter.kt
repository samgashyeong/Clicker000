package com.example.clicker.view.adapter

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.Image
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.clicker.R
import com.example.clicker.data.database.Setting
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


object BindingAdapter {

    @JvmStatic
    @BindingAdapter("app:setLeftButtonView")
    fun setLeftButtonView(button : Button, isChangeButton: MutableLiveData<Setting?>){
        if(isChangeButton.value?.isChangeButton == true){
            button.apply {
                text="-"
                setBackgroundResource(R.drawable.minus_selector)
            }
        }
        else{
            button.apply {
                text="+"
                setBackgroundResource(R.drawable.plus_selector)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:setScoredTextColor")
    fun setScoredTextColor(textView: TextView, string: String){
        val spannable = SpannableString(string)
        val stringArr = string.split(' ')

        spannable.setSpan(R.color.pressed_plus_color, 0, stringArr[0].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(R.color.pressed_minus_color, stringArr[0].length, stringArr[0].length+stringArr[1].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(R.color.default_text_color, stringArr[0].length+stringArr[1].length, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
    }

    @JvmStatic
    @BindingAdapter("app:setRightButton")
    fun setRightButton(button : Button, isChangeButton: MutableLiveData<Setting?>){
        if(isChangeButton.value?.isChangeButton == true){
            button.apply {
                text="+"
                setBackgroundResource(R.drawable.plus_selector)
            }
        }
        else{
            button.apply {
                text="-"
                setBackgroundResource(R.drawable.minus_selector)
            }
        }
    }


    @JvmStatic
    @BindingAdapter("app:plusOrMinusColor")
    fun setColor(textView : TextView, score: Int){
        if(score>=1){
            textView.setBackgroundResource(R.color.pressed_plus_color)
        }
        else{
            textView.setBackgroundResource(R.color.pressed_minus_color)
        }
    }

    @JvmStatic
    @BindingAdapter("app:link")
    fun setImage(imageView: ImageView, url : String){
        Glide.with(imageView.context).load(url).into(imageView)
    }
}