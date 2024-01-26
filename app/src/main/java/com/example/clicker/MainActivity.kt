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

        val viewModelFactory = MainViewModelFactory("", 0, 0, 0)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = this


        binding.plusButton.setOnClickListener {
            viewModel.plus.value = viewModel.plus.value?.plus(1)
            viewModel.total.value = viewModel.total.value?.plus(1)
        }

        binding.minusButton.setOnClickListener {
            viewModel.minus.value = viewModel.minus.value?.plus(-1)
            viewModel.total.value = viewModel.total.value?.plus(-1)
        }

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