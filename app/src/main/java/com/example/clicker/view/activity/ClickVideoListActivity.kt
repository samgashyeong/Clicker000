package com.example.clicker.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.view.adapter.ClickVideoAdapter

class ClickVideoListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClickVideoListBinding

    private var clickVideo : List<ClickVideoListWithClickInfo>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)


        val data = intent.intentSerializable("clickdata", ArrayList()) as ArrayList<ClickVideoListWithClickInfo>
        Log.d(TAG, "onCreate: ${data[0].videoInfo.snippet.title}")


        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@ClickVideoListActivity)
            adapter = ClickVideoAdapter(data){
                startActivity(Intent(this@ClickVideoListActivity, AnalyzeActivity::class.java).putExtra("data" ,it))
                //Toast.makeText(this@ClickVideoListActivity, it.videoInfo.snippet.title, Toast.LENGTH_SHORT).show()
            }
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

    private fun Intent.intentSerializable(key: String, data : ArrayList<ClickVideoListWithClickInfo>): ArrayList<ClickVideoListWithClickInfo>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, data::class.java)
        } else {
            this.getSerializableExtra(key) as ArrayList<ClickVideoListWithClickInfo>
        }
    }
}