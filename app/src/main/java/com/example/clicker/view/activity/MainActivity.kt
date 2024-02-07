package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityMainBinding
import com.example.clicker.view.dialog.DefaultDialog
import com.example.clicker.view.dialog.DefaultDialogDto
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.view.dialog.EditTextDialogDto
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
    private lateinit var startPointDialog : EditTextDialog
    private lateinit var databaseViewModel : MainDatabaseViewModel
    private lateinit var saveDataDialog : DefaultDialog
    private lateinit var initializeDialog : DefaultDialog

    private lateinit var tracker: YouTubePlayerTracker

    private lateinit var youtubePlayer : YouTubePlayer

    var sharedText : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        tracker = YouTubePlayerTracker()
        val viewModelFactory = MainViewModelFactory(tracker)

        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        databaseViewModel = ViewModelProvider(this, MainDatabaseViewModelFactory(application))[MainDatabaseViewModel::class.java]

        startPointDialog = EditTextDialog(this@MainActivity,
            EditTextDialogDto("Please enter the start point", "only use integer ex)10")){
            viewModel.startPoint.value = it.toFloat()
            startPointDialog.cancel()
        }

        saveDataDialog = DefaultDialog(this, DefaultDialogDto("Save Score Data", "Do you want to save the scored data?", "Save", "cancel")){
                if(viewModel.urlString.value != null){
                    databaseViewModel.insert(ClickVideoListWithClickInfo(viewModel.videoInfo.value!!,
                        viewModel.startPoint.value!!.toInt(),
                        viewModel.urlString.value!!,
                        viewModel.plus.value!!,
                        viewModel.minus.value!!,
                        viewModel.total.value!!,
                        viewModel.clickInfo.value!!
                    ))
                    Toast.makeText(this, "Data has been saved.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Bring on the Youtube video and Score them", Toast.LENGTH_SHORT).show()
                }
        }

        initializeDialog = DefaultDialog(this, DefaultDialogDto("Reset Scored Video", "Do you want to reset the scored data?", "Yes", "No")){
            viewModel.clickInfo.value?.clear()
            startPointDialog.show()
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        lifecycle.addObserver(binding.youtubePlayer)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        databaseViewModel.readAllData.observe(this, Observer {
            Log.d(TAG, "onCreate: ${databaseViewModel.readAllData.value?.size}")
        })

        binding.youtubePlayer.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youtubePlayer = youTubePlayer
                youTubePlayer.addListener(tracker)
            }
        })
        //인텐트 이벤트를 받아주는 곳 이걸 코드를 최적화를 하려면 어떻게 해야될까? 라는 고민을 할 필요가 있음.
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            startPointDialog.show()
        }



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

        viewModel.startPoint.observe(this, Observer {
            if (sharedText != null && viewModel.startPoint.value != null) {
                viewModel.urlString.value = viewModel.extractYouTubeVideoId(sharedText!!).value

                Log.d(TAG, "${viewModel.urlString.value!!}+${viewModel.startPoint!!.value!!}")
                binding.youtubeVideoTextView.visibility = View.INVISIBLE
                binding.youtubeButton.visibility = View.INVISIBLE
                binding.youtubeBlackView.visibility = View.INVISIBLE
                youtubePlayer.loadVideo(viewModel.urlString.value!!, viewModel.startPoint.value!!)
                //비디오 정보 가져오기
                viewModel.getVideoInfo(viewModel.urlString.value!!)

                viewModel.plus.value = 0
                viewModel.minus.value = 0
                viewModel.total.value = 0
            }
            else{
                Log.d(TAG, "onCreate: 예외실행")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayer.release()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.InitializingButton->{
                initializeDialog.show()
            }
            R.id.save->{
                saveDataDialog.show()
            }
            R.id.list->{
                val a  = databaseViewModel.readAllData.value
                startActivity(Intent(this, ClickVideoListActivity::class.java)
                    .putExtra("clickdata",a as ArrayList<ClickVideoListWithClickInfo>))
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
}
