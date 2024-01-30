package com.example.clicker.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.R
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityMainBinding
import com.example.clicker.view.dialog.StartPointDialog
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.example.clicker.viewmodel.MainDatabaseViewModelFactory
import com.example.clicker.viewmodel.MainViewModel
import com.example.clicker.viewmodel.MainViewModelFactory
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var startPointDialog : StartPointDialog
    private lateinit var databaseViewModel : MainDatabaseViewModel

    private lateinit var tracker: YouTubePlayerTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        tracker = YouTubePlayerTracker()

        val viewModelFactory = MainViewModelFactory(tracker)
        var sharedText : String? = null
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        databaseViewModel = ViewModelProvider(this, MainDatabaseViewModelFactory(application))[MainDatabaseViewModel::class.java]

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
                        youTubePlayer.loadVideo(viewModel.urlString.value!!, viewModel.startPoint.value!!)
                        youTubePlayer.addListener(tracker)
                    }
                })
                //비디오 정보 가져오기
                viewModel.getVideoInfo(viewModel.urlString.value!!)
            }
            else{
                Log.d(TAG, "onCreate: 예외실행")
            }
        })

        binding.saveButton.setOnClickListener {
            databaseViewModel.insert(ClickVideoListWithClickInfo(viewModel.videoInfo.value!!,
                    viewModel.urlString.value!!,
                    viewModel.plus.value!!,
                    viewModel.minus.value!!,
                    viewModel.total.value!!,
                viewModel.clickInfo.value!!
            ))

            databaseViewModel.getAll()
            Log.d(TAG, "onCreate: ${databaseViewModel.clickVideos!!.value?.get(0)!!.videoInfo.snippet.title}")
        }
//        viewModel.videoInfo!!.observe(this, Observer {
//            //유튜브
//            Log.d(TAG, "onCreate: ${viewModel.videoInfo.value!!.snippet.title}")
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayer.release()
    }
}
