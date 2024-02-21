package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityAnalyzeBinding
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.viewmodel.AnalyzeViewModel
import com.example.clicker.viewmodel.AnalyzeViewModelFactory
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAnalyzeBinding
    private lateinit var viewModel: AnalyzeViewModel
    private val databaseViewModel: MainDatabaseViewModel by viewModels()
    private lateinit var tracker: YouTubePlayerTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analyze)
        setContentView(binding.root)
        val data = intent.intentSerializable("data", ClickVideoListWithClickInfo::class.java)
        tracker = YouTubePlayerTracker()
        viewModel = ViewModelProvider(this, AnalyzeViewModelFactory(data!!, data.clickInfoList, data.videoId, tracker, 0))[AnalyzeViewModel::class.java]
        viewModel.videoInfo.value = data
        binding.data = viewModel.videoInfo.value
        //databaseViewModel = ViewModelProvider(this, MainDatabaseViewModelFactory(application))[MainDatabaseViewModel::class.java]


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.videoId.observe(this, Observer {
            Log.d(TAG, "onCreate: activity")
            binding.analyzeYoutubePlayer.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    youTubePlayer.loadVideo(viewModel.videoId.value!!, viewModel.videoInfo.value!!.startPoint.toFloat())
                    youTubePlayer.addListener(tracker)
                    startTracking()
                }
            })
        })


        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Score Information"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Statistics"))

        val navController = supportFragmentManager.findFragmentById(R.id.fragment_container)?.findNavController()
        navController?.navigate(R.id.action_clickInfoFragment2)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position) {
                        0 ->{
                            navController?.navigate(R.id.action_statisticsFragment2_to_clickInfoFragment2)
                            Log.d(TAG, "onTabSelected: ${tab.position}")
                        }
                        1 -> {
                            navController?.navigate(R.id.action_clickInfoFragment2_to_statisticsFragment2)
                            Log.d(TAG, "onTabSelected: ${tab.position}")
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun startTracking() {
        lifecycleScope.launch(Dispatchers.IO) {
            val secondList = viewModel.clickInfo.value!!.map {
                String.format("%.1f", it.clickSecond.toDouble()).toDouble()
            }
            //val secondList = viewModel.clickInfo.value!!.map { it.clickSecond.toInt() }
            while(true){
                val second = String.format("%.1f", tracker.currentSecond.toDouble()).toDouble()
                //Log.d(TAG, "startTracking: ${second}")
                if(tracker.state == PlayerConstants.PlayerState.PLAYING && secondList.contains(second)){
                    val nowPosition = secondList.indexOf(second)
                    viewModel.nowPosition.postValue(nowPosition)
                }
                delay(100L)
            }
        }
    }

    private fun Intent.intentSerializable(key: String, data: Class<ClickVideoListWithClickInfo>): ClickVideoListWithClickInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, data)
        } else {
            this.getSerializableExtra(key) as ClickVideoListWithClickInfo
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}