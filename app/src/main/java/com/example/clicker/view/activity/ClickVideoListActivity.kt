package com.example.clicker.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.MainDatabaseViewModel
import com.example.clicker.viewmodel.SearchVideoListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ClickVideoListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClickVideoListBinding
    private val databaseViewModel: MainDatabaseViewModel by viewModels()
    private val searchVideoListViewModel : SearchVideoListViewModel by viewModels()

    private var clickVideo : List<ClickVideoListWithClickInfo>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)
        databaseViewModel.readAllData.observe(this, Observer {
            searchVideoListViewModel.databaseScoredList.value = databaseViewModel.readAllData.value
            searchVideoListViewModel.searchList.value = databaseViewModel.readAllData.value
        })

        searchVideoListViewModel.searchList.observe(this, Observer {
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

        binding.toolbarEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                IME_ACTION_SEARCH -> {

                    if(binding.toolbarEditText.text.toString().isEmpty()){
                        searchVideoListViewModel.getAllVideos(databaseViewModel.readAllData.value)
                    }
                    else{
                        searchVideoListViewModel.findVideo(binding.toolbarEditText.text.toString())
                    }

                    binding.toolbarEditText.postDelayed({
                        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        manager.hideSoftInputFromWindow(
                            currentFocus!!.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS
                        )
                    }, 300)
                }
            }
            true
        })

        binding.toolbarEditText.addTextChangedListener {
            if(it.toString().isEmpty()){
                searchVideoListViewModel.getAllVideos(databaseViewModel.readAllData.value)
            }
            else{
                searchVideoListViewModel.findVideo(it.toString())
            }
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.example.clicker.R.menu.click_videos_menu , menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
                return true
            }
            R.id.search->{
                binding.toolbarTextView.visibility = View.INVISIBLE
                binding.toolbarEditText.visibility = View.VISIBLE

                binding.toolbarEditText.postDelayed({
                    binding.toolbarEditText.requestFocus()
                    val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.showSoftInput(binding.toolbarEditText, InputMethodManager.SHOW_IMPLICIT)
                }, 300)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}