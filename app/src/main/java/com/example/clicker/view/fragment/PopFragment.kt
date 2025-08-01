package com.example.clicker.view.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.clicker.R
import com.example.clicker.databinding.FragmentPopBinding
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PopFragment : Fragment() {
    
    private lateinit var binding: FragmentPopBinding
    private val viewModel: AnalyzeViewModel by activityViewModels()
    
    private var previousPosition = -1  // 이전 nowPosition 값
    private var previousPlusScore = -1  // -1로 초기화하여 첫 번째 데이터는 애니메이션하지 않음
    private var previousMinusScore = -1
    
    // 초기 기준점 (영상 시작 시점의 점수)
    private var initialPlusScore = 0
    private var initialMinusScore = 0
    private var isInitialized = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pop, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupPopFeatures()
        observeViewModel()
    }
    
    private fun setupPopFeatures() {
        // 초기 상태 설정 - 텍스트는 보이도록 설정하되 애니메이션은 시작하지 않음
        binding.plusText.alpha = 1f
        binding.minusText.alpha = 1f
        binding.plusText.translationY = 0f
        binding.minusText.translationY = 0f
        
        // 초기 텍스트 0으로 설정
        binding.plusText.text = "+0"
        binding.minusText.text = "-0"
    }
    
    private fun observeViewModel() {
        // nowPosition을 주요하게 관찰하여 위치 변화 시 애니메이션 트리거
        viewModel.nowPosition.observe(viewLifecycleOwner, Observer { position ->
            Log.d("PopFragment", "nowPosition changed: $position (previous: $previousPosition)")
            
            // 위치가 실제로 변경되었고, clickInfo가 존재하는 경우
            if (position != previousPosition && position >= 0) {
                val clickInfoList = viewModel.clickInfo.value
                val videoInfo = viewModel.videoInfo.value
                
                if (clickInfoList != null && position < clickInfoList.size && videoInfo != null) {
                    val currentClickInfo = clickInfoList[position]
                    val currentPlusTotal = currentClickInfo.plus
                    val currentMinusTotal = currentClickInfo.minus
                    
                    // 초기 기준점 설정 (첫 번째 데이터 로드 시)
                    if (!isInitialized) {
                        initialPlusScore = currentPlusTotal
                        initialMinusScore = currentMinusTotal
                        isInitialized = true
                        Log.d("PopFragment", "Initial scores set - Plus: $initialPlusScore, Minus: $initialMinusScore")
                    }
                    
                    // 초기값 대비 상대적인 점수 계산
                    val relativePlusScore = currentPlusTotal - initialPlusScore
                    val relativeMinusScore = currentMinusTotal - initialMinusScore
                    
                    Log.d("PopFragment", "Position $position - Raw Plus: $currentPlusTotal, Raw Minus: $currentMinusTotal")
                    Log.d("PopFragment", "Relative scores - Plus: $relativePlusScore, Minus: $relativeMinusScore")
                    Log.d("PopFragment", "Previous relative scores - Plus: $previousPlusScore, Minus: $previousMinusScore")
                    Log.d("PopFragment", "Plus comparison: $relativePlusScore > $previousPlusScore = ${relativePlusScore > previousPlusScore}")
                    Log.d("PopFragment", "Minus comparison: $relativeMinusScore < $previousMinusScore = ${relativeMinusScore < previousMinusScore}")
                    
                    // 첫 번째 로드가 아닌 경우에만 애니메이션 실행
                    if (previousPosition != -1) {
                        // 가점이 증가했는지 확인
                        if (relativePlusScore > previousPlusScore) {
                            Log.d("PopFragment", "Plus score increased from $previousPlusScore to $relativePlusScore at position $position!")
                            animatePlusText()
                        }
                        // 감점이 감소했는지 확인 (더 음수가 되었는지)
                        if (relativeMinusScore < previousMinusScore) {
                            Log.d("PopFragment", "Minus score decreased from $previousMinusScore to $relativeMinusScore at position $position!")
                            animateMinusText()
                        }
                        
                        // 디버깅: 점수 변화가 없는 경우도 로그 출력
                        if (relativePlusScore == previousPlusScore && relativeMinusScore == previousMinusScore) {
                            Log.d("PopFragment", "No score change at position $position")
                        }
                    } else {
                        Log.d("PopFragment", "First position load, skipping animation")
                    }
                    
                    // 이전 값들 업데이트 (상대적 점수로)
                    previousPlusScore = relativePlusScore
                    previousMinusScore = relativeMinusScore
                    
                    // UI 업데이트 - 상대적 점수 표시
                    binding.plusText.text = "+$relativePlusScore"
                    binding.minusText.text = "-${Math.abs(relativeMinusScore)}"  // 절댓값으로 표시
                }
                
                previousPosition = position
            }
        })
        
        // 실시간 점수 텍스트도 보조적으로 observe (백업용)
        viewModel.scoredText.observe(viewLifecycleOwner, Observer { scoreText ->
            Log.d("PopFragment", "Real-time score text: $scoreText")
        })
        
        // videoInfo 변화를 관찰 (데이터 로드 확인용)
        viewModel.videoInfo.observe(viewLifecycleOwner, Observer { videoInfo ->
            videoInfo?.let {
                Log.d("PopFragment", "VideoInfo loaded - Total clicks: ${it.clickInfoList.size}")
                // 새로운 비디오가 로드되면 초기화 상태 리셋
                isInitialized = false
                previousPosition = -1
                previousPlusScore = -1
                previousMinusScore = -1
                
                // 초기 텍스트 다시 설정
                binding.plusText.text = "+0"
                binding.minusText.text = "0"
            }
        })
        
        // 테스트용 버튼 클릭으로 애니메이션 테스트
        binding.testPlusButton.setOnClickListener {
            Log.d("PopFragment", "Test plus button clicked - testing animation")
            binding.plusText.text = "+5"  // 테스트용 텍스트 설정
            animatePlusText()
        }
        
        binding.testMinusButton.setOnClickListener {
            Log.d("PopFragment", "Test minus button clicked - testing animation")
            binding.minusText.text = "3"   // 테스트용 텍스트 설정 (마이너스 부호 없이)
            animateMinusText()
        }
    }
    
    private fun animatePlusText() {
        Log.d("PopFragment", "Starting plus animation")
        
        binding.plusText.apply {
            // 초기 상태 설정
            alpha = 1f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
            
            // 위로 올라가는 애니메이션
            val moveUpAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f, -300f)
            moveUpAnimator.duration = 800
            moveUpAnimator.interpolator = AccelerateDecelerateInterpolator()
            
            // 투명도 감소 애니메이션
            val fadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
            fadeOutAnimator.duration = 600
            fadeOutAnimator.startDelay = 200
            
            // 크기 애니메이션 (약간 커졌다가 작아짐)
            val scaleUpAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f)
            val scaleUpAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f)
            scaleUpAnimator.duration = 200
            scaleUpAnimatorY.duration = 200
            
            val scaleDownAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.2f, 1f)
            val scaleDownAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1.2f, 1f)
            scaleDownAnimator.duration = 200
            scaleDownAnimatorY.duration = 200
            scaleDownAnimator.startDelay = 200
            scaleDownAnimatorY.startDelay = 200
            
            // 애니메이션 시작
            moveUpAnimator.start()
            fadeOutAnimator.start()
            scaleUpAnimator.start()
            scaleUpAnimatorY.start()
            scaleDownAnimator.start()
            scaleDownAnimatorY.start()
            
            // 애니메이션 종료 후 원래 상태로 복귀
            fadeOutAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    translationY = 0f
                    alpha = 1f  // 다시 보이도록 설정
                    scaleX = 1f
                    scaleY = 1f
                    Log.d("PopFragment", "Plus animation ended")
                }
            })
        }
    }
    
    private fun animateMinusText() {
        Log.d("PopFragment", "Starting minus animation")
        Log.d("PopFragment", "MinusText current state - alpha: ${binding.minusText.alpha}, translationY: ${binding.minusText.translationY}")
        
        binding.minusText.apply {
            // 초기 상태 설정
            alpha = 1f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
            
            // 위로 올라가는 애니메이션
            val moveUpAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f, -300f)
            moveUpAnimator.duration = 800
            moveUpAnimator.interpolator = AccelerateDecelerateInterpolator()
            
            // 투명도 감소 애니메이션
            val fadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
            fadeOutAnimator.duration = 600
            fadeOutAnimator.startDelay = 200
            
            // 크기 애니메이션 (약간 커졌다가 작아짐)
            val scaleUpAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f)
            val scaleUpAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f)
            scaleUpAnimator.duration = 200
            scaleUpAnimatorY.duration = 200
            
            val scaleDownAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.2f, 1f)
            val scaleDownAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1.2f, 1f)
            scaleDownAnimator.duration = 200
            scaleDownAnimatorY.duration = 200
            scaleDownAnimator.startDelay = 200
            scaleDownAnimatorY.startDelay = 200
            
            // 애니메이션 시작
            moveUpAnimator.start()
            fadeOutAnimator.start()
            scaleUpAnimator.start()
            scaleUpAnimatorY.start()
            scaleDownAnimator.start()
            scaleDownAnimatorY.start()
            
            // 애니메이션 종료 후 원래 상태로 복귀
            fadeOutAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    translationY = 0f
                    alpha = 1f  // 다시 보이도록 설정
                    scaleX = 1f
                    scaleY = 1f
                    Log.d("PopFragment", "Minus animation ended")
                }
            })
        }
    }
    
    companion object {
        fun newInstance(): PopFragment {
            return PopFragment()
        }
    }
}
