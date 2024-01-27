package com.example.clicker

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.databinding.ActivityMainBinding
import com.example.clicker.dialog.StartPointDialog
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var startPointDialog : StartPointDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewModelFactory = MainViewModelFactory()
        var sharedText : String? = null
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        startPointDialog = StartPointDialog(this@MainActivity, viewModel)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        lifecycle.addObserver(binding.youtubePlayer)



        //인텐트 이벤트를 받아주는 곳 이걸 코드를 최적화를 하려면 어떻게 해야될까? 라는 고민을 할 필요가 있음.
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

            startPointDialog.show()
        }


        viewModel.startPoint!!.observe(this, Observer {
            if (sharedText != null && viewModel.startPoint.value != null) {
                viewModel.urlString!!.value = viewModel.extractYouTubeVideoId(sharedText).value

                Log.d(TAG, "${viewModel.urlString!!.value!!}+${viewModel.startPoint!!.value!!}")

                binding.youtubePlayer.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        Log.d(TAG, "onReady: 아아아아아아아아")
                        youTubePlayer.loadVideo(viewModel.urlString.value!!, viewModel.startPoint.value!!)
                    }
                })
            }
            else{
                Log.d(TAG, "onCreate: 예외실행")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayer.release()
    }
}