package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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
    
    // 이전 위치 추적용
    private var previousPosition = -1
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
            Log.d(TAG, "AnalyzeActivity - scoredText changed: '$scoreText'")
            scoreText?.let {
                // 점수 텍스트 파싱 (형식: "plus minus total")
                val parts = it.trim().split(" ")
                Log.d(TAG, "AnalyzeActivity - scoredText parts: ${parts.joinToString(", ")}")
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
                        val displayText = "+$relativePlusScore -${Math.abs(relativeMinusScore)} $relativeTotalScore"
                        binding.toolbarTotal.text = displayText
                        
                        Log.d(TAG, "AnalyzeActivity - Raw scores: +$currentPlusScore $currentMinusScore $currentTotalScore")
                        Log.d(TAG, "AnalyzeActivity - Relative scores: +$relativePlusScore ${Math.abs(relativeMinusScore)} $relativeTotalScore")
                        Log.d(TAG, "AnalyzeActivity - Toolbar updated to: '$displayText'")
                        
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "AnalyzeActivity - Error parsing score text: '$it'", e)
                        binding.toolbarTotal.text = "+0 -0 0"  // 오류 시 기본값
                    }
                } else {
                    Log.w(TAG, "AnalyzeActivity - Invalid score text format: '$it' (expected 3 parts, got ${parts.size})")
                    binding.toolbarTotal.text = "+0 -0 0"  // 파싱 실패 시 기본값
                }
            }
        }
        
        // 새로운 비디오가 로드되면 초기화 상태 리셋
        viewModel.videoInfo.observe(this) { videoInfo ->
            videoInfo?.let {
                Log.d(TAG, "AnalyzeActivity - VideoInfo loaded, resetting initialization")
                isInitialized = false
                previousPosition = -1
                binding.toolbarTotal.text = "+0 0 0"  // 초기 텍스트 설정
            }
        }
        
        // nowPosition을 관찰하여 clickScorePoint 기반 테두리 애니메이션 실행
        viewModel.nowPosition.observe(this, Observer { position ->
            Log.d(TAG, "AnalyzeActivity - nowPosition changed: $position (previous: $previousPosition)")
            
            // 위치가 실제로 변경된 경우 (position == -1인 경우도 처리)
            if (position != previousPosition) {
                if (position == -1) {
                    // 첫 번째 클릭 전 상태 - 애니메이션 없음
                    Log.d(TAG, "AnalyzeActivity - Before first click, no animation")
                } else if (position >= 0) {
                    val clickInfoList = viewModel.clickInfo.value
                    
                    Log.d(TAG, "AnalyzeActivity - clickInfoList size: ${clickInfoList?.size}, position: $position")
                    
                    if (clickInfoList != null && position < clickInfoList.size) {
                        val currentClickInfo = clickInfoList[position]
                        val clickScorePoint = currentClickInfo.clickScorePoint
                        
                        Log.d(TAG, "AnalyzeActivity - Position $position - clickScorePoint: $clickScorePoint, isLast: ${position == clickInfoList.size - 1}")
                        
                        // 위치가 변경된 경우 애니메이션 실행
                        // previousPosition과 다른 경우에만 애니메이션 (단, -1에서 0으로 변경되는 첫 번째 클릭은 제외)
                        if (previousPosition != position && !(previousPosition == -1 && position == 0)) {
                            // clickScorePoint로 가점/감점 판별하여 테두리 애니메이션 실행
                            if (clickScorePoint > 0) {
                                // 가점 발생 - 초록색 테두리 애니메이션
                                Log.d(TAG, "AnalyzeActivity - Plus point detected! Score: +$clickScorePoint (position: $position)")
                                animateBorderColor(true)
                            } else if (clickScorePoint < 0) {
                                // 감점 발생 - 빨간색 테두리 애니메이션  
                                Log.d(TAG, "AnalyzeActivity - Minus point detected! Score: $clickScorePoint (position: $position)")
                                animateBorderColor(false)
                            } else {
                                Log.d(TAG, "AnalyzeActivity - Neutral click (clickScorePoint: 0) at position: $position")
                            }
                            // clickScorePoint가 0인 경우는 애니메이션 없음
                        } else {
                            Log.d(TAG, "AnalyzeActivity - First position load, skipping animation")
                        }
                    } else {
                        Log.e(TAG, "AnalyzeActivity - clickInfo is null or position out of bounds! clickInfoList: ${clickInfoList?.size}, position: $position")
                    }
                }
                
                previousPosition = position
            }
        })
    }
    
    /**
     * 테두리 색상 애니메이션 실행 (영상은 고정, 테두리만 애니메이션)
     * @param isPlus true면 초록색(가점), false면 빨간색(감점)
     */
    private fun animateBorderColor(isPlus: Boolean) {
        val targetDrawable = if (isPlus) {
            ContextCompat.getDrawable(this, R.drawable.player_border_plus)
        } else {
            ContextCompat.getDrawable(this, R.drawable.player_border_minus)
        }
        
        val defaultDrawable = ContextCompat.getDrawable(this, R.drawable.player_border_default)
        
        Log.d(TAG, "Starting border animation - isPlus: $isPlus")
        
        // 기존 애니메이션이 실행 중이면 정리
        binding.borderOverlay.clearAnimation()
        binding.borderOverlay.animate().cancel()
        
        // 초기 상태로 리셋
        binding.borderOverlay.scaleX = 1f
        binding.borderOverlay.scaleY = 1f
        binding.borderOverlay.alpha = 1f
        
        // 테두리 색상 변경
        binding.borderOverlay.background = targetDrawable
        
        // 한 번만 깜빡임 (총 0.3초)
        binding.borderOverlay.animate()
            .alpha(0.2f)
            .setDuration(150) // 0.15초 - 밝음에서 어두움
            .withEndAction {
                binding.borderOverlay.animate()
                    .alpha(0f)
                    .setDuration(150) // 0.15초 - 어두움에서 완전 투명
                    .withEndAction {
                        // 완전히 투명한 상태로 복원
                        binding.borderOverlay.background = defaultDrawable
                        binding.borderOverlay.alpha = 1f
                        Log.d(TAG, "Border flash animation completed - restored to transparent")
                    }
                    .start()
            }
            .start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 애니메이션 정리
        binding.borderOverlay.clearAnimation()
        binding.borderOverlay.animate().cancel()
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