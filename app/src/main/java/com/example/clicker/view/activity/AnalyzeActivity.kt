package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atwa.filepicker.core.FilePicker
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityAnalyzeBinding
import com.example.clicker.util.PermissionHelper
import com.example.clicker.view.adapter.SidebarVideoAdapter
import com.example.clicker.view.dialog.DefaultDialog
import com.example.clicker.view.dialog.DefaultDialogDto
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel
import com.example.clicker.viewmodel.score_list.SearchVideoListViewModel
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAnalyzeBinding
    private val viewModel: AnalyzeViewModel by viewModels()
    private val searchViewModel: SearchVideoListViewModel by viewModels()
    private lateinit var saveDataDialog: DefaultDialog
    
    // 사이드바 관련 변수들
    private lateinit var sidebarAdapter: SidebarVideoAdapter
    private var allVideos: List<ClickVideoListWithClickInfo> = emptyList()
    
    // 파일 관련 변수들
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var filePicker: FilePicker
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    
    // 검색 상태 관리
    private var isSearchMode = false
    
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
            setupVideoPlayer()
        } else {
            // 데이터가 없는 경우 - 에러 메시지 표시
            showNoVideoInfoMessage()
        }

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
        
        // 저장 다이얼로그 초기화
        initializeSaveDialog()
        
        // 사이드바 초기화
        initializeSidebar()
        
        // 파일 관련 초기화
        initializeFileHandlers()
        
        // BackPressed 콜백 설정
        setupBackPressedCallback()
    }
    
    private fun initializeSaveDialog() {
        saveDataDialog = DefaultDialog(
            this,
            DefaultDialogDto(
                "Save Video",
                "Do you want to save the video?",
                "Yes",
                "No"
            )
        ) {
            // 비디오 정보가 있는 경우에만 저장
            val videoInfo = viewModel.videoInfo.value
            if (videoInfo != null) {
                viewModel.insertVideoData(
                    success = {
                        Toast.makeText(this, "Video has been saved.", Toast.LENGTH_SHORT).show()
                        saveDataDialog.cancel() // 다이얼로그 즉시 닫기
                        refreshSidebarData() // 사이드바 데이터 새로고침
                    },
                    failed = {
                        Toast.makeText(this, "Failed to save video data.", Toast.LENGTH_SHORT).show()
                        saveDataDialog.cancel() // 실패해도 다이얼로그 닫기
                    }
                )
            } else {
                Toast.makeText(this, "No video data to save.", Toast.LENGTH_SHORT).show()
                saveDataDialog.cancel() // 데이터가 없어도 다이얼로그 닫기
            }
        }
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
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.analyze_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
                return true
            }
            R.id.saveVideo -> {
                saveDataDialog.show()
                return true
            }
            R.id.videoList -> {
                binding.drawerLayout.openDrawer(GravityCompat.END)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupVideoPlayer() {
        // YouTube 플레이어 표시 및 초기화
        binding.analyzeYoutubePlayer.visibility = android.view.View.VISIBLE
        binding.noVideoInfoText.visibility = android.view.View.GONE
        binding.borderOverlay.visibility = android.view.View.VISIBLE
        
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
    }
    
    private fun showNoVideoInfoMessage() {
        // 비디오 정보가 없을 때 메시지 표시
        binding.analyzeYoutubePlayer.visibility = android.view.View.GONE
        binding.noVideoInfoText.visibility = android.view.View.VISIBLE
        binding.borderOverlay.visibility = android.view.View.GONE
        
        // 기본 초기 상태로 툴바 설정
        binding.toolbarTotal.text = "+0 -0 0"
        
        Log.d(TAG, "No video information available - showing error message")
    }
    
    private fun initializeSidebar() {
        // 사이드바 RecyclerView 설정
        sidebarAdapter = SidebarVideoAdapter(emptyList()) { clickedVideo ->
            // 비디오 클릭 시 해당 비디오로 분석 화면 갱신
            loadVideoFromSidebar(clickedVideo)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        
        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AnalyzeActivity)
            adapter = sidebarAdapter
        }
        
        // 저장된 비디오 목록 로드
        loadSavedVideos()
        
        // 검색 기능 설정
        setupSearchFunctionality()
        
        // 툴바 버튼 리스너 설정
        setupSidebarToolbar()
    }
    
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    isSearchMode -> {
                        // 검색 모드일 때는 검색 모드 종료
                        toggleSearchMode()
                    }
                    binding.drawerLayout.isDrawerOpen(GravityCompat.END) -> {
                        // 사이드바가 열려있을 때는 사이드바 닫기
                        binding.drawerLayout.closeDrawer(GravityCompat.END)
                    }
                    else -> {
                        // 그 외에는 액티비티 종료
                        finish()
                    }
                }
            }
        })
    }
    
    private fun loadSavedVideos() {
        // SearchViewModel의 LiveData 관찰
        searchViewModel.searchList.observe(this) { videos ->
            videos?.let {
                allVideos = it
                updateSidebarList(it)
            }
        }
    }
    
    private fun refreshSidebarData() {
        // SearchViewModel에서 데이터베이스로부터 최신 데이터를 다시 로드
        searchViewModel.refreshData()
    }
    
    private fun updateSidebarList(videos: List<ClickVideoListWithClickInfo>) {
        if (videos.isEmpty()) {
            binding.noDataText.visibility = View.VISIBLE
            binding.sidebarRecyclerView.visibility = View.GONE
        } else {
            binding.noDataText.visibility = View.GONE
            binding.sidebarRecyclerView.visibility = View.VISIBLE
            sidebarAdapter.updateData(videos)
        }
    }
    
    private fun setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                searchViewModel.getAllVideos()
            } else {
                searchViewModel.findVideo(query)
            }
        }
        
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 키보드 숨기기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
        
        // 포커스 리스너 추가
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isSearchMode && binding.searchEditText.text.toString().isEmpty()) {
                toggleSearchMode()
            }
        }
    }
    
    private fun loadVideoFromSidebar(clickedVideo: ClickVideoListWithClickInfo) {
        Log.d(TAG, "Loading video from sidebar: ${clickedVideo.videoInfo?.snippet?.title}")
        Log.d(TAG, "Video data - ID: ${clickedVideo.videoId}, StartPoint: ${clickedVideo.startPoint}")
        Log.d(TAG, "ClickInfo count: ${clickedVideo.clickInfoList.size}")
        
        // 대안 방법: 새로운 AnalyzeActivity 시작
        val intent = Intent(this, AnalyzeActivity::class.java)
        intent.putExtra("data", clickedVideo)
        startActivity(intent)
        finish() // 현재 Activity 종료
        
        // 원래 방법 (주석 처리)
        /*
        // 선택된 비디오로 분석 화면 갱신
        viewModel.videoInfo.value = clickedVideo
        viewModel.setVideo(clickedVideo)
        
        Log.d(TAG, "ViewModel videoId after setVideo: ${viewModel.videoId.value}")
        
        // 새로운 비디오로 플레이어 재설정
        if (clickedVideo.videoInfo != null && clickedVideo.videoId.isNotEmpty()) {
            // 플레이어 재초기화
            reinitializeVideoPlayer(clickedVideo)
        } else {
            Log.w(TAG, "Video info is null or video ID is empty")
            showNoVideoInfoMessage()
        }
        */
        
        Toast.makeText(this, "Loading video: ${clickedVideo.videoInfo?.snippet?.title ?: "Unknown"}", Toast.LENGTH_SHORT).show()
    }
    
    private fun reinitializeVideoPlayer(videoData: ClickVideoListWithClickInfo) {
        Log.d(TAG, "Reinitializing video player for: ${videoData.videoInfo?.snippet?.title}")
        Log.d(TAG, "Video ID: ${videoData.videoId}, Start Point: ${videoData.startPoint}")
        
        // MainActivity의 방식을 정확히 따라해보기
        val container = binding.playerContainer
        
        // 기존 플레이어 완전히 제거
        binding.analyzeYoutubePlayer.release()
        container.removeView(binding.analyzeYoutubePlayer)
        
        // 새로운 YouTubePlayerView 생성 (MainActivity와 동일한 방식)
        val newYouTubePlayerView = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView(this)
        newYouTubePlayerView.enableAutomaticInitialization = false
        
        // 컨테이너에 추가
        container.addView(newYouTubePlayerView, 0)
        
        // 가시성 설정
        newYouTubePlayerView.visibility = View.VISIBLE
        binding.noVideoInfoText.visibility = View.GONE
        binding.borderOverlay.visibility = View.VISIBLE
        
        // MainActivity와 동일한 초기화 방식
        newYouTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                Log.d(TAG, "New YouTube player ready")
                viewModel.youtubePlayer.value = youTubePlayer
                youTubePlayer.addListener(viewModel.tracker)
            }
            
            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Log.e(TAG, "YouTube player error: $error")
                Toast.makeText(this@AnalyzeActivity, "Error loading video: $error", Toast.LENGTH_SHORT).show()
            }
        })
        
        // MainActivity와 동일한 비디오 로드 방식
        newYouTubePlayerView.getYouTubePlayerWhenReady(object : com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                val videoId = videoData.videoId
                val startPoint = videoData.startPoint.toFloat()
                
                Log.d(TAG, "onYouTubePlayer callback: loading $videoId at $startPoint")
                
                if (videoId.isNotEmpty()) {
                    youTubePlayer.loadVideo(videoId, startPoint)
                } else {
                    Log.e(TAG, "Video ID is empty")
                    Toast.makeText(this@AnalyzeActivity, "Video ID is missing", Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        Log.d(TAG, "Video player reinitialization completed")
    }
    
    private fun initializeFileHandlers() {
        permissionHelper = PermissionHelper(this)
        filePicker = FilePicker.getInstance(this)
        
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 파일 선택 결과 처리
                Log.d(TAG, "File selection result: ${result.data}")
            }
        }
    }
    
    private fun setupSidebarToolbar() {
        binding.searchButton.setOnClickListener {
            toggleSearchMode()
        }
        
        binding.moreButton.setOnClickListener {
            showMoreOptions()
        }
    }
    
    private fun toggleSearchMode() {
        isSearchMode = !isSearchMode
        
        if (isSearchMode) {
            // 검색 모드 활성화
            binding.sidebarTitle.visibility = View.INVISIBLE
            binding.searchEditText.visibility = View.VISIBLE
            
            binding.searchEditText.postDelayed({
                binding.searchEditText.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        } else {
            // 검색 모드 비활성화
            binding.sidebarTitle.visibility = View.VISIBLE
            binding.searchEditText.visibility = View.INVISIBLE
            binding.searchEditText.setText("")
            
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            
            // 전체 목록 표시
            searchViewModel.getAllVideos()
        }
    }
    
    private fun showMoreOptions() {
        // 더보기 옵션 메뉴 표시
        val popup = androidx.appcompat.widget.PopupMenu(this, binding.moreButton)
        popup.menuInflater.inflate(R.menu.sidebar_more_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.import_data -> {
                    importDataFromFile()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun importDataFromFile() {
        filePicker.pickMimeFile("application/json") { fileMeta ->
            fileMeta?.let {
                Log.d(TAG, "Selected file: ${it.file?.readText()}")
                searchViewModel.insertAll(it)
                Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

}