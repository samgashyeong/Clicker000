package com.example.clicker.view.activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
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
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.util.PermissionHelper
import com.example.clicker.util.PermissionHelper.Companion.REQUEST_CODE
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.SearchVideoListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException


@AndroidEntryPoint
class ClickVideoListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClickVideoListBinding
    private val searchVideoListViewModel : SearchVideoListViewModel by viewModels()

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var filePicker : FilePicker

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)
        permissionHelper = PermissionHelper(this)
        filePicker = FilePicker.getInstance(this)

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

        //Log.d(TAG, "onCreate: ${File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "testFile.txt").readText()}")

        checkPermission()

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
    private fun saveTextToFile(text: String) {
        // Get the external storage directory for documents
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create a new file in the documents directory
        val file = File(documentsDir, "sampleTextFile.txt")

        try {
            // Write the text to the file
            file.writeText(text)
            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
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
                Log.d(TAG, "onOptionsItemSelected: ${File(Environment.getExternalStoragePublicDirectory(
                    DIRECTORY_DOWNLOADS), "sampleTextFile.txt").readText()}")
                Log.d(TAG, "onOptionsItemSelected: ${File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "sampleTextFile.txt").toPath()}")
                saveTextToFile("Hello, Lee jun sang")
            }
            R.id.folder->{
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.setDataAndType(Uri.parse("content://media/external/file"), "*/*")
//                startActivity(intent)

                filePicker.pickFile {
                    Log.d(TAG, "onOptionsItemSelected: ${it!!.name}  ${it!!.file!!.path}")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveTextToFile("Hello, this is a sample text!")
                saveTextToFile("Hello, Lee jun sang")
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            saveTextToFile("Hello, this is a sample text!")
        }
    }
}