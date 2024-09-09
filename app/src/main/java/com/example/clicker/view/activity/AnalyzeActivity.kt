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
import androidx.navigation.fragment.findNavController
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityAnalyzeBinding
import com.example.clicker.viewmodel.AnalyzeViewModel
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAnalyzeBinding
    private val viewModel: AnalyzeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analyze)
        setContentView(binding.root)
        val data = intent.intentSerializable("data", ClickVideoListWithClickInfo::class.java)
        viewModel.videoInfo.value = data
        binding.data = viewModel

        viewModel.videoInfo.value = data!!
        viewModel.videoInfo.observe(this, Observer {
            if(viewModel.videoInfo.value != null){
                viewModel.scoredText.value = viewModel.videoInfo.value!!.plusScore.toString() + " " + viewModel.videoInfo.value!!.minusScore.toString() + " " + viewModel.videoInfo.value!!.totalScore.toString()
                viewModel.clickInfo.value = data.clickInfoList
                viewModel.videoId.value = data.videoId
                viewModel.dataToEntry()
                binding.analyzeYoutubePlayer.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        viewModel.youtubePlayer.value = youTubePlayer
                        youTubePlayer.loadVideo(
                            viewModel.videoId.value!!,
                            viewModel.videoInfo.value!!.startPoint.toFloat()
                        )
                        youTubePlayer.addListener(viewModel.tracker)
                    }
                })
                viewModel.startTracking()
                viewModel.startAddDataEntry()
            }
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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