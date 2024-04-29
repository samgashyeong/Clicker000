package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.example.clicker.view.dialog.DefaultDialog
import com.example.clicker.view.dialog.DefaultDialogDto
import com.example.clicker.view.dialog.DialogManager
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.view.dialog.EditTextDialogDto
import com.example.clicker.view.dialog.SaveDialog
import com.example.clicker.view.dialog.SettingDialog
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.example.clicker.viewmodel.MainViewModel
import com.example.clicker.viewmodel.SettingDataStoreViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val databaseViewModel : MainDatabaseViewModel by viewModels()
    private val dataStoreViewModel: SettingDataStoreViewModel by viewModels()

    private lateinit var startPointDialog : EditTextDialog
    private lateinit var saveDataDialog : DefaultDialog
    private lateinit var initializeDialog : DefaultDialog
    private lateinit var settingDialog: SettingDialog
    private lateinit var saveDialog: SaveDialog

    @Inject
    lateinit var dialogManager: DialogManager

    private lateinit var youtubePlayerView : YouTubePlayerView
    private lateinit var vibrator: Vibrator
    private var sharedText : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setDialog()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        binding.viewModel = viewModel
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
                viewModel.youTubePlayer.value = youTubePlayer
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
        startPointDialog = EditTextDialog(this@MainActivity,
            EditTextDialogDto("Please enter the start point", "only use integer ex)10")){
            if(it.toIntOrNull() == null){
                Toast.makeText(this, "Please enter an integer!", Toast.LENGTH_SHORT).show()
            }
            else{
                dialogManager.closeAllDialog()
                viewModel.startPoint.value = it.toFloat()
                startPointDialog.cancel()
            }
        }

        settingDialog = SettingDialog(this, dataStoreViewModel)

        saveDataDialog = DefaultDialog(this, DefaultDialogDto("Save Score Data", "Do you want to save the scored data?", "Save", "cancel")){
            Log.d(TAG, "setDialog: ${viewModel.urlString.value}")
            if(viewModel.urlString.value!!.isNotEmpty() && dataStoreViewModel.isChangeButton.value == true){
                databaseViewModel.insert(ClickVideoListWithClickInfo(viewModel.videoInfo.value!!,
                    viewModel.startPoint.value!!.toInt(),
                    viewModel.urlString.value!!,
                    viewModel.minus.value!!,
                    viewModel.plus.value!!,
                    viewModel.total.value!!,
                    viewModel.clickInfo.value!!
                ))
                Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
            }
            else if(viewModel.urlString.value!!.isNotEmpty() && (dataStoreViewModel.isChangeButton.value == false || dataStoreViewModel.isChangeButton.value == null)){
                databaseViewModel.insert(ClickVideoListWithClickInfo(viewModel.videoInfo.value!!,
                    viewModel.startPoint.value!!.toInt(),
                    viewModel.urlString.value!!,
                    viewModel.plus.value!!,
                    viewModel.minus.value!!,
                    viewModel.total.value!!,
                    viewModel.clickInfo.value!!
                ))
                Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Bring on the Youtube video and Score them", Toast.LENGTH_SHORT).show()
            }
            dialogManager.closeAllDialog()
        }

        initializeDialog = DefaultDialog(this, DefaultDialogDto("Reset Scored Video", "Do you want to reset the scored data?", "Yes", "No")){
            if(viewModel.urlString.value?.isNotEmpty() == true){
                viewModel.clickInfo.value?.clear()
                dialogManager.closeAllDialog()
                startPointDialog.show()
            }
            else{
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

    private fun setObserve(){
        dataStoreViewModel.isChangeButton.observe(this, Observer {
            viewModel.swapPlusAndMinus()
            Log.e(TAG, "setObserve: 에러처리", )
//            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                // 현재 세로 모드입니다.
//            } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                // 현재 가로 모드입니다.
//            }
        })


//        dataStoreViewModel.isVibButton.observe(this, Observer {
//            viewModel.swapPlusAndMinus()
//        })
        
        
        viewModel.vib.observe(this, Observer {
            if(viewModel.vib.value == true){
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(40);

                viewModel.vib.value = false
            }
        })

        viewModel.stopActivityVideoSecond.observe(this, Observer {
            if(viewModel.isStartVideo.value == true) {
                binding.frameLayout.removeView(youtubePlayerView)

                val youtubePlayerView = YouTubePlayerView(this)
                binding.frameLayout.addView(youtubePlayerView)

                youtubePlayerView.enableAutomaticInitialization = false
                youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        viewModel.youTubePlayer.value = youTubePlayer
                        youTubePlayer.addListener(viewModel.tracker)
                    }
                })

                binding.youtubeVideoTextView.visibility = View.INVISIBLE
                binding.youtubeButton.visibility = View.INVISIBLE
                binding.youtubeBlackView.visibility = View.INVISIBLE

                youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        viewModel.youTubePlayer.value = youTubePlayer
                        youTubePlayer.loadVideo(viewModel.urlString.value!!,
                            viewModel.stopActivityVideoSecond.value!!.toFloat())
                    }
                })
            }
        })

        viewModel.startPoint.observe(this, Observer {
            if (sharedText != null && viewModel.startPoint.value != null) {
                viewModel.urlString.value = viewModel.extractYouTubeVideoId(sharedText!!).value
                binding.youtubeVideoTextView.visibility = View.INVISIBLE
                binding.youtubeButton.visibility = View.INVISIBLE
                binding.youtubeBlackView.visibility = View.INVISIBLE

                viewModel.youTubePlayer.value!!.loadVideo(viewModel.urlString.value!!, viewModel.startPoint.value!!)
                viewModel.isStartVideo.value = true

                viewModel.plus.value = 0
                viewModel.minus.value = 0
                viewModel.total.value = 0
                viewModel.clickInfo.value?.clear()
                //비디오 정보 가져오기

                //youtube api key 가져오기
                val ai: ApplicationInfo = applicationContext.packageManager
                    .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                val value = ai.metaData?.getString("youtubeApi")
                val key = value.toString()
                Log.d(TAG, "setObserve: ${key}")
                viewModel.getVideoInfo(viewModel.urlString.value!!, key)
            }
            else{
                Log.d(TAG, "onCreate: 예외실행")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.SettingButton->{
                settingDialog.show()
            }
            R.id.save->{
                saveDialog.show()
            }
            R.id.list->{
                startActivity(Intent(this, ClickVideoListActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.example.clicker.R.menu.main_menu , menu)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            if (intent.getAction().equals(Intent.ACTION_SEND) && "text/plain".equals(intent.getType())) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                // startPointDialog 관련 처리
                startPointDialog.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(viewModel.isStartVideo.value == true){
            viewModel.stopActivityVideoSecond.value = viewModel.tracker.currentSecond.toInt()
        }
        viewModel.youTubePlayer.value?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
//        if(dataStoreViewModel.isChangeButton.value == true){
//            viewModel.swapPlusAndMinus()
//        }
        viewModel.swapPlusAndMinus()
        youtubePlayerView.release()
    }
}
