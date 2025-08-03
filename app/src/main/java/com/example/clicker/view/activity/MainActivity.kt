package com.example.clicker.view.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.clicker.R
import com.example.clicker.databinding.ActivityMainBinding
import com.example.clicker.util.Mode
import com.example.clicker.util.RankingDto
import com.example.clicker.view.activity.AnalyzeActivity
import com.example.clicker.view.activity.ClickVideoListActivity
import com.example.clicker.view.dialog.DefaultDialog
import com.example.clicker.view.dialog.DefaultDialogDto
import com.example.clicker.view.dialog.DialogManager
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.view.dialog.EditTextDialogDto
import com.example.clicker.view.dialog.RankingDialog
import com.example.clicker.view.dialog.SaveDialog
import com.example.clicker.view.dialog.SavePlayerEditTextDialog
import com.example.clicker.view.dialog.SettingDialog
import com.example.clicker.viewmodel.main.MainActivityViewModel
import com.google.android.material.navigation.NavigationView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel : MainActivityViewModel by viewModels()

    private lateinit var startPointDialog: EditTextDialog
    private lateinit var saveDataDialog: DefaultDialog
    private lateinit var initializeDialog: DefaultDialog
    private lateinit var settingDialog: SettingDialog
    private lateinit var saveDialog: SaveDialog
    private lateinit var savePlayerEditTextDialog: SavePlayerEditTextDialog
    private lateinit var rankingDialog: RankingDialog
    private var youtubePlayer: YouTubePlayer? = null

    @Inject
    lateinit var dialogManager: DialogManager

    private lateinit var youtubePlayerView: YouTubePlayerView
    private var sharedText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Log.d(TAG, "onCreate: ${intent.getStringExtra(Intent.EXTRA_TEXT)}")
        setDialog()

        binding.viewModel1 = viewModel
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setObserve()

        youtubePlayerView = YouTubePlayerView(this)
        youtubePlayerView.enableAutomaticInitialization = false
        binding.frameLayout.addView(youtubePlayerView)
        youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youtubePlayer = youTubePlayer
                youTubePlayer.addListener(viewModel.tracker)
            }
        })

        binding.youtubeButton.setOnClickListener {
            val url = "https://www.youtube.com"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            intent.setPackage("com.google.android.youtube")
            if (intent.resolveActivity(packageManager) == null) {
                intent.setPackage(null)
            }

            startActivity(intent)
        }

        if (savedInstanceState == null) {
            settingIntent(intent)
        }

        // NavigationView 설정
        binding.navigationView?.setNavigationItemSelectedListener(this)
        
        // BackPressedCallback 설정
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout?.isDrawerOpen(GravityCompat.END) == true) {
                    binding.drawerLayout?.closeDrawer(GravityCompat.END)
                } else {
                    finish()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        settingIntent(intent)
    }

    private fun setDialog() {
        startPointDialog = EditTextDialog(
            this@MainActivity,
            EditTextDialogDto("Please enter the start point", "only use integer ex)10")
        ) {
            Log.d(TAG, "setDialog: ${it}")
            if (it.toIntOrNull() == null) {
                Toast.makeText(this, "Please enter an integer!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "setDialog: ${it} ${sharedText}asdfaffffff")
                viewModel.apply {
                    changeStartPoint(it.toFloat())
                    extractYouTubeVideoId(sharedText!!)
                    clearClickInfo()
                    clearScoreData()
                    getVideoInfo()
                    changeVideo(true)
                }

                // 가시성 설정
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        setDefaultModeViewWhenVideoStarted()
                    }
                    is Mode.Ranking -> {
                        setRankingModeView()
                    }
                }

                youtubePlayer?.loadVideo(
                    viewModel.videoScoreUiModel.value!!.videoId,
                    viewModel.videoScoreUiModel.value!!.startPoint
                )

                startPointDialog.cancel()
                dialogManager.closeAllDialog()
            }
        }
        savePlayerEditTextDialog = SavePlayerEditTextDialog(
            this@MainActivity,
            EditTextDialogDto("Write Player Name", "ex ) Lee Jun Sang")
        ) {
            viewModel.addPlayer(RankingDto(it, viewModel.videoScoreUiModel.value!!.plus, viewModel.videoScoreUiModel.value!!.minus, viewModel.videoScoreUiModel.value!!.total)){
                Toast.makeText(this, "now 1st \n${viewModel.ranking.value?.get(0)!!.name} ${viewModel.ranking.value?.get(0)!!.plus} ${viewModel.ranking.value?.get(0)!!.minus} ${viewModel.ranking.value?.get(0)!!.total}", Toast.LENGTH_SHORT).show()
            }
            savePlayerEditTextDialog.cancel()
        }
        rankingDialog = RankingDialog(
            this@MainActivity,
            rankingList = viewModel.ranking.value!!,
            copyCallback = {
                val text = viewModel.convertDataToText()

                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Ranking Data", text)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            clearCallback = {
                viewModel.clearRankingData()
                rankingDialog.cancel()
            }
        )
        settingDialog = SettingDialog(this, viewModel){
            viewModel.clearScoreData()
            Toast.makeText(this, "The data has been reset.", Toast.LENGTH_SHORT).show()
        }

        saveDataDialog = DefaultDialog(
            this,
            DefaultDialogDto(
                "Save Score Data",
                "Do you want to save the scored data?",
                "Save",
                "cancel"
            )
        ) {
            viewModel.insertVideoData(
                success = {
                    Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
                },
                failed = {
                    Toast.makeText(
                        this,
                        "Bring on the Youtube video and Score them",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            dialogManager.closeAllDialog()
        }
        initializeDialog = DefaultDialog(
            this,
            DefaultDialogDto(
                "Reset Scored Video",
                "Do you want to reset the scored data?",
                "Yes",
                "No"
            )
        ) {
            if (viewModel.videoScoreUiModel.value!!.videoId.isNotEmpty()) {
                //viewModel1.clickInfo.value?.clear()
                viewModel.apply {
                    clearScoreData()
                    clearClickInfo()
                }
                dialogManager.closeAllDialog()
                startPointDialog.show()
            } else {
                Toast.makeText(this, "Please bring the Youtube video!", Toast.LENGTH_SHORT).show()
                dialogManager.closeAllDialog()
            }
        }

        saveDialog = SaveDialog(this, initializeDialog, saveDataDialog)

        dialogManager.dialogs.add(saveDialog)
        dialogManager.dialogs.add(initializeDialog)
        dialogManager.dialogs.add(saveDataDialog)
        dialogManager.dialogs.add(startPointDialog)
        dialogManager.dialogs.add(settingDialog)
    }

    private fun findClickFile(activity: AppCompatActivity, fileName: String, content: String) {
        val resolver = activity.contentResolver
        Log.d(TAG, "saveTextToFile: ${Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS).path}")
        MediaScannerConnection.scanFile(baseContext, arrayOf("${Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS).path}/dataJunsang.json"), null) { path, uri ->
            // 파일이 스캔된 후 콜백에서 결과를 처리합니다.
            if (uri != null) {
                Log.d(TAG, "saveTextToFile: ${path} ${uri}")// 파일이 성공적으로 스캔되었을 때 URI 반환
                uri.let {
                    resolver.delete(uri, null, null)
                }
            } else {
                Log.d(TAG, "saveTextToFile: ")  // 스캔 실패 시 null 반환
            }
        }
    }
    private fun setObserve() {
        viewModel.settingUiModel.observe(this, Observer {
            if(it.isChangeButton){
                viewModel.leftMinusRightPlus()
            }
            else{
                viewModel.leftPlusRightMinus()
            }

            when (it.mode) {
                is Mode.Default -> {
                    if(!viewModel.videoScoreUiModel.value!!.isVideoStart){
                        setDefaultModeViewWhenVideoNotStarted()
                    }
                    else{
                        setDefaultModeViewWhenVideoStarted()
                    }
                }
                is Mode.Ranking -> {
                    setRankingModeView()
                    youtubePlayer?.pause()
                    viewModel.apply {
                        clearScoreData()
                        clearClickInfo()
                    }
                }
            }
            
            // 모드가 변경될 때마다 메뉴 아이콘을 업데이트
            invalidateOptionsMenu()
        })

        viewModel.ranking.observe(this) {
            rankingDialog = RankingDialog(
                this@MainActivity,
                rankingList = it,
                copyCallback = {
                    val text = viewModel.convertDataToText()

                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Ranking Data", text)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(this, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                clearCallback = {
                    viewModel.clearRankingData()
                    Toast.makeText(this, "Data has been deleted.", Toast.LENGTH_SHORT).show()
                    rankingDialog.cancel()
                }
            )
        }


        viewModel.stopActivityVideoSecond.observe(this){
            Log.d(TAG, "setObserve: onNewintent")
            if (viewModel.videoScoreUiModel.value!!.isVideoStart) {
                binding.frameLayout.removeView(youtubePlayerView)

                val youtubePlayerView = YouTubePlayerView(this)
                binding.frameLayout.addView(youtubePlayerView)

                youtubePlayerView.enableAutomaticInitialization = false
                youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        youtubePlayer = youTubePlayer
                        youTubePlayer.addListener(viewModel.tracker)
                    }
                })
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        setDefaultModeViewWhenVideoStarted()
                    }
                    is Mode.Ranking -> {
                        setRankingModeView()
                        youtubePlayer?.pause()
                    }
                }


                if(viewModel.settingUiModel.value!!.mode == Mode.Default()){
                    youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                            youtubePlayer = youTubePlayer
                            Log.d(TAG, "onYouTubePlayer: ${viewModel.videoScoreUiModel.value!!.videoId}")
                            youTubePlayer.loadVideo(
                                viewModel.videoScoreUiModel.value!!.videoId,
                                viewModel.stopActivityVideoSecond.value!!.toFloat()
                            )
                        }
                    })
                }
            }
        }
    }

    // 비디오가 시작되지 않았을 때 Mode.Default 관련 가시성 설정
    private fun setDefaultModeViewWhenVideoNotStarted() {
        Log.d(TAG, "setDefaultModeViewWhenVideoNotStarted: ")
        binding.youtubeBlackView.visibility = View.VISIBLE
        binding.youtubeButton.visibility = View.VISIBLE
        binding.youtubeVideoTextView.visibility = View.VISIBLE
        binding.frameLayout.visibility = View.INVISIBLE
        binding.youtubePlayer.visibility = View.INVISIBLE
    }

    // 비디오가 시작된 후 Mode.Default 관련 가시성 설정
    private fun setDefaultModeViewWhenVideoStarted() {
        Log.d(TAG, "setDefaultModeViewWhenVideoStarted: ")
        binding.youtubeBlackView.visibility = View.INVISIBLE
        binding.youtubeButton.visibility = View.INVISIBLE
        binding.youtubeVideoTextView.visibility = View.INVISIBLE
        binding.frameLayout.visibility = View.VISIBLE
        binding.youtubePlayer.visibility = View.INVISIBLE
    }

    // Mode.Ranking 관련 가시성 설정
    private fun setRankingModeView() {
        Log.d(TAG, "setRankingModeView: ")
        binding.youtubeBlackView.visibility = View.GONE
        binding.youtubeButton.visibility = View.GONE
        binding.youtubeVideoTextView.visibility = View.GONE
        binding.frameLayout.visibility = View.GONE
        binding.youtubePlayer.visibility = View.GONE
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.modeToggle -> {
                // 모드 토글 기능
                val currentMode = viewModel.settingUiModel.value!!.mode
                when (currentMode) {
                    is Mode.Default -> {
                        viewModel.setMode(Mode.Ranking())
                        Toast.makeText(this, "Ranking Mode", Toast.LENGTH_SHORT).show()
                    }
                    is Mode.Ranking -> {
                        viewModel.setMode(Mode.Default())
                        Toast.makeText(this, "Default Mode", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.resetData -> {
                // 모드에 따라 다른 동작
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        // 기본 모드일 때는 리셋 기능
                        viewModel.clearScoreData()
                        viewModel.clearClickInfo()
                        Toast.makeText(this, "데이터가 리셋되었습니다", Toast.LENGTH_SHORT).show()
                    }
                    is Mode.Ranking -> {
                        // 랭킹 모드일 때는 플레이어 추가 기능
                        savePlayerEditTextDialog.show()
                    }
                }
            }

            R.id.chartView -> {
                // 모드에 따라 다른 동작
                when (viewModel.settingUiModel.value?.mode) {
                    is Mode.Ranking -> {
                        // 랭킹 모드일 때는 랭킹 다이얼로그 표시
                        rankingDialog.show()
                    }
                    else -> {
                        // 디폴트 모드일 때는 Analyze 화면으로 이동
                        val analysisData = viewModel.getCurrentAnalysisData()
                        startActivity(Intent(this, AnalyzeActivity::class.java).putExtra("data", analysisData))
                    }
                }
            }

            R.id.settings -> {
                // 설정 다이얼로그 표시
                settingDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                settingDialog.show()
                binding.drawerLayout?.closeDrawer(GravityCompat.END)
            }
            R.id.nav_video_list -> {
                startActivity(Intent(this, ClickVideoListActivity::class.java))
                binding.drawerLayout?.closeDrawer(GravityCompat.END)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.example.clicker.R.menu.main_menu, menu)
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        
        // 설정 아이콘 흰색으로 변경
        val settingsItem = menu?.findItem(R.id.settings)
        settingsItem?.icon?.setTint(ContextCompat.getColor(this, R.color.white))
        
        // 랭킹 모드일 때 리셋 아이콘을 플러스 아이콘으로 변경
        val resetItem = menu?.findItem(R.id.resetData)
        resetItem?.let { item ->
            when (viewModel.settingUiModel.value?.mode) {
                is Mode.Ranking -> {
                    // 랭킹 모드일 때는 플러스 아이콘으로 변경
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_add_white)
                    item.title = "Add Player"
                }
                else -> {
                    // 기본 모드일 때는 리셋 아이콘
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_reset)
                    item.title = "리셋"
                }
            }
        }
        
        return true
    }

    private fun settingIntent(intent : Intent?) {
        Log.d(TAG, "settingIntent: ${intent!!.getStringExtra(Intent.EXTRA_TEXT)}")
        if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            if (intent.getAction()
                    .equals(Intent.ACTION_SEND) && "text/plain".equals(intent.getType())
            ) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                // startPointDialog 관련 처리
                when (viewModel.settingUiModel.value?.mode) {
                    is Mode.Default -> {
                        Log.d(TAG, "test : ${sharedText}")
                        if(viewModel.settingUiModel.value!!.setStartPoint){
                            startPointDialog.show()
                        }
                        else if(!viewModel.settingUiModel.value!!.setStartPoint){
                            viewModel.apply {
                                changeStartPoint(0f)
                                extractYouTubeVideoId(sharedText!!)
                                clearClickInfo()
                                clearScoreData()
                                getVideoInfo()
                                changeVideo(true)
                            }
                            when (viewModel.settingUiModel.value!!.mode) {
                                is Mode.Default -> {
                                    setDefaultModeViewWhenVideoStarted()
                                }
                                is Mode.Ranking -> {
                                    setRankingModeView()
                                }
                            }

                            Log.d(TAG, "onNewIntent: ${viewModel.videoScoreUiModel.value!!.startPoint}")
                            youtubePlayer?.loadVideo(
                                viewModel.videoScoreUiModel.value!!.videoId,
                                0f
                            )

                            startPointDialog.cancel()
                            dialogManager.closeAllDialog()
                        }
                    }
                    is Mode.Ranking -> {
                        Toast.makeText(
                            this,
                            "Youtube video is not supported in ranking mode.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    null -> {}
                }
            }
        }

        binding.viewModel1 = viewModel
        binding.lifecycleOwner = this
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.videoScoreUiModel.value!!.isVideoStart == true) {
            Log.d(TAG, "onStop: ${viewModel.tracker.currentSecond.toInt()}")
            viewModel.changeStopPoint(viewModel.tracker.currentSecond.toInt())
        }
        youtubePlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayerView.release()
    }
}