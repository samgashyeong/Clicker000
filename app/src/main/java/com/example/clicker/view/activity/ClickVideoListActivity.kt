package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.MainDatabaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClickVideoListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClickVideoListBinding
    private val databaseViewModel: MainDatabaseViewModel by viewModels()
    private var clickVideo : List<ClickVideoListWithClickInfo>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)
        databaseViewModel.readAllData.observe(this, Observer {
            binding.recycler.apply {
                layoutManager = LinearLayoutManager(this@ClickVideoListActivity)
                adapter = it?.let {
                    ClickVideoAdapter(it){
                        startActivity(Intent(this@ClickVideoListActivity, AnalyzeActivity::class.java).putExtra("data" ,it))
                    }
                }
                if(it!!.size != 0){
                    binding.alarmText.visibility = View.INVISIBLE
                }
            }
        })

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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