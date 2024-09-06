package com.example.clicker.view.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityMainBinding
import com.example.clicker.util.RankingDto
import com.example.clicker.view.dialog.DefaultDialog
import com.example.clicker.view.dialog.DefaultDialogDto
import com.example.clicker.view.dialog.DialogManager
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.view.dialog.EditTextDialogDto
import com.example.clicker.view.dialog.RankingDialog
import com.example.clicker.view.dialog.SaveDialog
import com.example.clicker.view.dialog.SavePlayerEditTextDialog
import com.example.clicker.view.dialog.SettingDialog
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.example.clicker.viewmodel.MainViewModel
import com.example.clicker.viewmodel.Mode
import com.example.clicker.viewmodel.SettingDataStoreViewModel
import com.example.clicker.viewmodel.main.MainActivityViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel1: MainViewModel by viewModels()
    private val databaseViewModel: MainDatabaseViewModel by viewModels()
    private val dataStoreViewModel: SettingDataStoreViewModel by viewModels()
    private val viewModel : MainActivityViewModel by viewModels()

    private lateinit var startPointDialog: EditTextDialog
    private lateinit var saveDataDialog: DefaultDialog
    private lateinit var initializeDialog: DefaultDialog
    private lateinit var settingDialog: SettingDialog
    private lateinit var saveDialog: SaveDialog
    private lateinit var savePlayerEditTextDialog: SavePlayerEditTextDialog
    private lateinit var rankingDialog: RankingDialog
    private lateinit var youtubePlayer: YouTubePlayer

    @Inject
    lateinit var dialogManager: DialogManager

    private lateinit var youtubePlayerView: YouTubePlayerView
    private var sharedText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setDialog()

        binding.viewModel1 = viewModel
        binding.viewModel = viewModel1
        binding.databaseStore = dataStoreViewModel
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
                viewModel1.youTubePlayer.value = youTubePlayer
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
                Log.d(TAG, "setDialog: ${it}asdfaffffff")
                viewModel.changeStartPoint(it.toFloat())
                viewModel.extractYouTubeVideoId(sharedText!!)
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        binding.youtubeBlackView.visibility = View.INVISIBLE
                        binding.youtubeButton.visibility = View.INVISIBLE
                        binding.youtubeVideoTextView.visibility = View.INVISIBLE
                        binding.frameLayout.visibility = View.VISIBLE
                        binding.youtubePlayer.visibility = View.INVISIBLE
                    }
                    is Mode.Ranking -> {
                        binding.youtubeBlackView.visibility = View.GONE
                        binding.youtubeButton.visibility = View.GONE
                        binding.youtubeVideoTextView.visibility = View.GONE
                        binding.frameLayout.visibility = View.GONE

                        binding.youtubePlayer.visibility = View.GONE
                    }
                }
                //비디오 정보 가져오기

                Log.d(TAG, "setDialog: test logd")
                Log.d(TAG, "setDialog: ${it}")
                youtubePlayer.loadVideo(
                    viewModel.videoScoreUiModel.value!!.videoId,
                    viewModel.videoScoreUiModel.value!!.startPoint
                )

                viewModel.apply {
                    clearClickInfo()
                    clearScoreData()
                    getVideoInfo()
                }

                //youtube api key 가져오기
                /*val ai: ApplicationInfo = applicationContext.packageManager
                    .getApplicationInfo(
                        applicationContext.packageName,
                        PackageManager.GET_META_DATA
                    )
                val value = ai.metaData?.getString("youtubeApi")
                val key = value.toString()
                Log.d(TAG, "setObserve: ${key}")*/
                //viewModel1.getVideoInfo(viewModel1.urlString.value!!, key)
                startPointDialog.cancel()
                dialogManager.closeAllDialog()
            }
        }
        savePlayerEditTextDialog = SavePlayerEditTextDialog(
            this@MainActivity,
            EditTextDialogDto("Write Player Name", "ex ) Lee Jun Sang")
        ) {
            viewModel1.addPlayer(RankingDto(it, viewModel1.plus.value!!, viewModel1.minus.value!!, viewModel1.total.value!!)){
                Toast.makeText(this, "now 1st \n${viewModel1.ranking.value?.get(0)!!.name} ${viewModel1.ranking.value?.get(0)!!.plus} ${viewModel1.ranking.value?.get(0)!!.minus} ${viewModel1.ranking.value?.get(0)!!.total}", Toast.LENGTH_SHORT).show()
            }
            savePlayerEditTextDialog.cancel()

        }
        rankingDialog = RankingDialog(
            this@MainActivity,
            rankingList = viewModel1.ranking.value!!,
            copyCallback = {
                val text = viewModel1.convertDataToText()

                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Ranking Data", text)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            clearCallback = {
                viewModel1.clearRankingData()
                rankingDialog.cancel()
            }
        )
        settingDialog = SettingDialog(this, viewModel){
            viewModel1.plus.value = 0
            viewModel1.minus.value = 0
            viewModel1.total.value = 0
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
            if (viewModel1.urlString.value!!.isNotEmpty() && dataStoreViewModel.isChangeButton.value == true) {
                databaseViewModel.insert(
                    ClickVideoListWithClickInfo(
                        viewModel1.videoInfo.value!!,
                        viewModel1.startPoint.value!!.toInt(),
                        viewModel1.urlString.value!!,
                        viewModel1.minus.value!!,
                        viewModel1.plus.value!!,
                        viewModel1.total.value!!,
                        viewModel1.clickInfo.value!!
                    )
                )
                Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
            } else if (viewModel1.urlString.value!!.isNotEmpty() && (dataStoreViewModel.isChangeButton.value == false || dataStoreViewModel.isChangeButton.value == null)) {
                databaseViewModel.insert(
                    ClickVideoListWithClickInfo(
                        viewModel1.videoInfo.value!!,
                        viewModel1.startPoint.value!!.toInt(),
                        viewModel1.urlString.value!!,
                        viewModel1.plus.value!!,
                        viewModel1.minus.value!!,
                        viewModel1.total.value!!,
                        viewModel1.clickInfo.value!!
                    )
                )
                Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Bring on the Youtube video and Score them",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            if (viewModel1.urlString.value?.isNotEmpty() == true) {
                viewModel1.clickInfo.value?.clear()
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
                    binding.youtubeBlackView.visibility = View.VISIBLE
                    binding.youtubeButton.visibility = View.VISIBLE
                    binding.youtubeVideoTextView.visibility = View.VISIBLE
                    binding.frameLayout.visibility = View.INVISIBLE
                    binding.youtubePlayer.visibility = View.INVISIBLE
                    viewModel1.youTubePlayer.value?.pause()
                }

                is Mode.Ranking -> {
                    binding.youtubeBlackView.visibility = View.GONE
                    binding.youtubeButton.visibility = View.GONE
                    binding.youtubeVideoTextView.visibility = View.GONE
                    binding.frameLayout.visibility = View.GONE
                    binding.youtubePlayer.visibility = View.GONE
                    viewModel1.youTubePlayer.value?.pause()
                }
            }
        })

        viewModel1.ranking.observe(this) {
            rankingDialog = RankingDialog(
                this@MainActivity,
                rankingList = it,
                copyCallback = {
                    val text = viewModel1.convertDataToText()

                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Ranking Data", text)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(this, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                clearCallback = {
                    viewModel1.clearRankingData()
                    rankingDialog.cancel()
                }
            )
        }


        viewModel1.stopActivityVideoSecond.observe(this, Observer {
            if (viewModel1.isStartVideo.value == true) {
                binding.frameLayout.removeView(youtubePlayerView)

                val youtubePlayerView = YouTubePlayerView(this)
                binding.frameLayout.addView(youtubePlayerView)

                youtubePlayerView.enableAutomaticInitialization = false
                youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        viewModel1.youTubePlayer.value = youTubePlayer
                        youTubePlayer.addListener(viewModel1.tracker)
                    }
                })
                when (dataStoreViewModel.mode.value!!) {
                    is Mode.Default -> {
                        binding.youtubeBlackView.visibility = View.INVISIBLE
                        binding.youtubeButton.visibility = View.INVISIBLE
                        binding.youtubeVideoTextView.visibility = View.INVISIBLE
                        binding.frameLayout.visibility = View.VISIBLE
                        binding.youtubePlayer.visibility = View.INVISIBLE
                    }
                    is Mode.Ranking -> {
                        binding.youtubeBlackView.visibility = View.GONE
                        binding.youtubeButton.visibility = View.GONE
                        binding.youtubeVideoTextView.visibility = View.GONE
                        binding.frameLayout.visibility = View.GONE

                        binding.youtubePlayer.visibility = View.GONE
                        viewModel1.youTubePlayer.value?.pause()
                    }
                }


                if(dataStoreViewModel.mode.value!! == Mode.Default()){
                    youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                            viewModel1.youTubePlayer.value = youTubePlayer
                            youTubePlayer.loadVideo(
                                viewModel1.urlString.value!!,
                                viewModel1.stopActivityVideoSecond.value!!.toFloat()
                            )
                        }
                    })
                }
            }
        })

//        viewModel1.startPoint.observe(this, Observer {
//            if (sharedText != null && viewModel1.startPoint.value != null) {
//                viewModel1.urlString.value = viewModel1.extractYouTubeVideoId(sharedText!!).value
//
//                when (dataStoreViewModel.mode.value!!) {
//                    is Mode.Default -> {
//                        binding.youtubeBlackView.visibility = View.INVISIBLE
//                        binding.youtubeButton.visibility = View.INVISIBLE
//                        binding.youtubeVideoTextView.visibility = View.INVISIBLE
//                        binding.frameLayout.visibility = View.VISIBLE
//                        binding.youtubePlayer.visibility = View.INVISIBLE
//                    }
//
//                    is Mode.Ranking -> {
//                        binding.youtubeBlackView.visibility = View.GONE
//                        binding.youtubeButton.visibility = View.GONE
//                        binding.youtubeVideoTextView.visibility = View.GONE
//                        binding.frameLayout.visibility = View.GONE
//
//                        binding.youtubePlayer.visibility = View.GONE
//                    }
//                }
//
//                viewModel1.youTubePlayer.value!!.loadVideo(
//                    viewModel1.urlString.value!!,
//                    viewModel1.startPoint.value!!
//                )
//                viewModel1.isStartVideo.value = true
//                viewModel1.plus.value = 0
//                viewModel1.minus.value = 0
//                viewModel1.total.value = 0
//                viewModel1.clickInfo.value?.clear()
//                //비디오 정보 가져오기
//
//                //youtube api key 가져오기
//                val ai: ApplicationInfo = applicationContext.packageManager
//                    .getApplicationInfo(
//                        applicationContext.packageName,
//                        PackageManager.GET_META_DATA
//                    )
//                val value = ai.metaData?.getString("youtubeApi")
//                val key = value.toString()
//                Log.d(TAG, "setObserve: ${key}")
//                viewModel1.getVideoInfo(viewModel1.urlString.value!!, key)
//            }
//        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.SettingButton -> {
                settingDialog.show()
            }

            R.id.save -> {
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        saveDialog.show()
                    }

                    is Mode.Ranking -> {
                        savePlayerEditTextDialog.show()
                    }

                }
            }

            R.id.list -> {
                when (viewModel.settingUiModel.value!!.mode) {
                    is Mode.Default -> {
                        startActivity(Intent(this, ClickVideoListActivity::class.java))
                    }

                    is Mode.Ranking -> {
                        rankingDialog.show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.example.clicker.R.menu.main_menu, menu)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            if (intent.getAction()
                    .equals(Intent.ACTION_SEND) && "text/plain".equals(intent.getType())
            ) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                // startPointDialog 관련 처리
                when (viewModel.settingUiModel.value?.mode) {
                    is Mode.Default -> {
                        startPointDialog.show()
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
    }

    override fun onStop() {
        super.onStop()
        if (viewModel1.isStartVideo.value == true) {
            viewModel1.stopActivityVideoSecond.value = viewModel1.tracker.currentSecond.toInt()
        }
        viewModel1.youTubePlayer.value?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayerView.release()
    }
}