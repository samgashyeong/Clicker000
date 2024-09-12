package com.example.clicker.view.activity

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
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
import com.example.clicker.databinding.ActivityClickVideoListBinding
import com.example.clicker.util.PermissionHelper
import com.example.clicker.util.PermissionHelper.Companion.REQUEST_CODE
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.SearchVideoListViewModel
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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_video_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_click_video_list)
        permissionHelper = PermissionHelper(this)
        filePicker = FilePicker.getInstance(this)
        file_path = getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()

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
    fun findFileUri(resolver: ContentResolver, fileName: String): android.net.Uri? {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        val queryUri = MediaStore.Files.getContentUri("external")

        resolver.query(queryUri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return ContentUris.withAppendedId(queryUri, id)
            }
        }

        return null
    }

    private fun findClickFile(activity: AppCompatActivity, fileName: String, content: String) {
        val resolver = baseContext.contentResolver
        Log.d(TAG, "saveTextToFile: ${Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS).path}")
        MediaScannerConnection.scanFile(baseContext, arrayOf("${Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS).path}/dataJunsang.json"), null) { path, uri ->
            // 파일이 스캔된 후 콜백에서 결과를 처리합니다.
            if (uri != null) {
                Log.d(TAG, "saveTextToFile: ${path} ${uri}")// 파일이 성공적으로 스캔되었을 때 URI 반환
                uri.let {
                    resolver.delete(uri, null, null)
                }
                saveClickFile(fileName, content)
            } else {
                Log.d(TAG, "saveTextToFile: ")  // 스캔 실패 시 null 반환
                saveClickFile(fileName, content)
            }
        }
    }

    private fun saveClickFile(fileName: String, content: String) {
        val resolver = baseContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val newUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        newUri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use {
                it.write(content.toByteArray())
                it.flush()
            }
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
                findClickFile(this, "dataJunsang.json", "테스트")
            }
            R.id.folder->{
                filePicker.pickMimeFile("application/json"){
                    it
                    Log.d(TAG, "onOptionsItemSelected: ${it?.file?.readText()}")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findClickFile(this, "dataJunsang", "테스트")
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
            findClickFile(this, "dataJunsang", "테스트")
        }
    }
}