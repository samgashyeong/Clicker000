package com.example.clicker.view.activity

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.atwa.filepicker.core.FilePicker
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.util.PermissionHelper
import com.example.clicker.util.PermissionHelper.Companion.REQUEST_CODE
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.SearchVideoListViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.OutputStream
import kotlin.random.Random


@AndroidEntryPoint
class ClickVideoListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClickVideoListBinding
    private val searchVideoListViewModel : SearchVideoListViewModel by viewModels()

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var filePicker : FilePicker
    private lateinit var file_path : String
    private lateinit var activityResultLauncher2 : ActivityResultLauncher<Intent>
    private lateinit var delete : ActivityResultLauncher<IntentSenderRequest>
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)
        permissionHelper = PermissionHelper(this)
        filePicker = FilePicker.getInstance(this)
        file_path = getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()

        delete = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("deleteResultLauncher", "Android 11 or higher : deleted")
            }
        }


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

        activityResultLauncher2 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {


                when (it.resultCode) {

                    RESULT_OK -> {
                        val des2 = contentResolver.openFileDescriptor(
                            it.data?.data!!,
                            "r"
                        ) // it.data?.data!!(사용자가 쓰기를 원하는 파일의 경로)를 가지고 file descriptor를 만듬
                        val fis = FileInputStream(des2?.fileDescriptor) //파일 접근 스트림 생성
                        val dis = DataInputStream(fis)

                        val data1 = dis.readInt()
                        val data2 = dis.readDouble()
                        val data3 = dis.readBoolean()
                        val data4 = dis.readUTF()

                        dis.close()


                    }

                }
            }

        //Log.d(TAG, "onCreate: ${File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "testFile.txt").readText()}")

        binding.toolbarEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                IME_ACTION_SEARCH -> {

                    if(binding.toolbarEditText.text.toString().isEmpty()){
                        searchVideoListViewModel.getAllVideos()
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
                searchVideoListViewModel.getAllVideos()
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
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            R.id.folder->{
                filePicker.pickMimeFile("application/json"){
                    it
                    Log.d(TAG, "onOptionsItemSelected: ${it?.file?.readText()}")
                    searchVideoListViewModel.insertAll(listOf())
                    val listType = object : TypeToken<List<ClickVideoListWithClickInfo>>() {}.type
                   searchVideoListViewModel.insertAll(
                       Gson().fromJson(it?.file?.readText(), listType)
                   )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}