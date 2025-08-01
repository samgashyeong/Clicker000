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
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAnalyzeBinding
    private val viewModel: AnalyzeViewModel by viewModels()
    
    // 초기 기준점 (영상 시작 시점의 점수)
    private var initialPlusScore = 0
    private var initialMinusScore = 0
    private var initialTotalScore = 0
    private var isInitialized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analyze)
        setContentView(binding.root)
        val data = intent.intentSerializable("data", ClickVideoListWithClickInfo::class.java)
        viewModel.videoInfo.value = data
        binding.data = viewModel

        if (data != null) {
            // MainActivity에서 데이터를 전달받은 경우
            viewModel.setVideo(data)
        } else {
            // 데이터가 없는 경우 데이터베이스에서 가져오기
            viewModel.initializeFromDatabase()
        }
        
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

        setObserve()


        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Score Information"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Statistics"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pop Analysis"))

        val navController = supportFragmentManager.findFragmentById(R.id.fragment_container)?.findNavController()
        // 초기에는 첫 번째 탭(Score Information)으로 이동
        navController?.navigate(R.id.clickInfoFragment2)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position) {
                        0 ->{
                            navController?.navigate(R.id.clickInfoFragment2)
                            Log.d(TAG, "onTabSelected: ${tab.position}")
                        }
                        1 -> {
                            navController?.navigate(R.id.statisticsFragment2)
                            Log.d(TAG, "onTabSelected: ${tab.position}")
                        }
                        2 -> {
                            navController?.navigate(R.id.popFragment)
                            Log.d(TAG, "onTabSelected: Pop Fragment ${tab.position}")
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

    private fun setObserve() {
        viewModel.scoredText.observe(this) { scoreText ->
            scoreText?.let {
                // 점수 텍스트 파싱 (형식: "plus minus total")
                val parts = it.trim().split(" ")
                if (parts.size >= 3) {
                    try {
                        val currentPlusScore = parts[0].toInt()
                        val currentMinusScore = parts[1].toInt()
                        val currentTotalScore = parts[2].toInt()
                        
                        // 초기 기준점 설정 (첫 번째 데이터 로드 시)
                        if (!isInitialized) {
                            initialPlusScore = currentPlusScore
                            initialMinusScore = currentMinusScore
                            initialTotalScore = currentTotalScore
                            isInitialized = true
                            Log.d(TAG, "AnalyzeActivity - Initial scores set - Plus: $initialPlusScore, Minus: $initialMinusScore, Total: $initialTotalScore")
                        }
                        
                        // 초기값 대비 상대적인 점수 계산
                        val relativePlusScore = currentPlusScore - initialPlusScore
                        val relativeMinusScore = currentMinusScore - initialMinusScore
                        val relativeTotalScore = currentTotalScore - initialTotalScore
                        
                        // UI 업데이트 - 상대적 점수 표시
                        binding.toolbarTotal.text = "+$relativePlusScore ${Math.abs(relativeMinusScore)} $relativeTotalScore"
                        
                        Log.d(TAG, "AnalyzeActivity - Raw scores: +$currentPlusScore $currentMinusScore $currentTotalScore")
                        Log.d(TAG, "AnalyzeActivity - Relative scores: +$relativePlusScore ${Math.abs(relativeMinusScore)} $relativeTotalScore")
                        
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "AnalyzeActivity - Error parsing score text: $it", e)
                        binding.toolbarTotal.text = "+0 0 0"  // 오류 시 기본값
                    }
                } else {
                    binding.toolbarTotal.text = "+0 0 0"  // 파싱 실패 시 기본값
                }
            }
        }
        
        // 새로운 비디오가 로드되면 초기화 상태 리셋
        viewModel.videoInfo.observe(this) { videoInfo ->
            videoInfo?.let {
                Log.d(TAG, "AnalyzeActivity - VideoInfo loaded, resetting initialization")
                isInitialized = false
                binding.toolbarTotal.text = "+0 0 0"  // 초기 텍스트 설정
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