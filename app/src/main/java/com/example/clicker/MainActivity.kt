package com.example.clicker

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.clicker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewModelFactory = MainViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = this



        //인텐트 이벤트를 받아주는 곳 이걸 코드를 최적화를 하려면 어떻게 해야될까? 라는 고민을 할 필요가 있음.
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText : String? = intent.getStringExtra(Intent.EXTRA_TEXT)
            viewModel.minus.value = 0
            viewModel.plus.value = 0
            viewModel.total.value = 0


            var youtubeVideoID: MutableLiveData<String> = MutableLiveData()
            if (sharedText != null) {
                Log.d(TAG, "onCreate: ${sharedText}")
                youtubeVideoID.value = viewModel.extractYouTubeVideoId(sharedText).value
            }
            viewModel.urlString.value = youtubeVideoID.value
        }
    }
}