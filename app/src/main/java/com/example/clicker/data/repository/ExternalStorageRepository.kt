package com.example.clicker.data.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.util.Log
import com.example.clicker.util.CLICKER000_EXTERNAL_FILE_NAME
import java.io.OutputStream

class ExternalStorageRepository(private val context: Context) {

    fun findClickFile(content: String, externalFileDate: String) {
        val resolver = context.contentResolver

        MediaScannerConnection.scanFile(context, arrayOf("${
            Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS
            ).path}/${CLICKER000_EXTERNAL_FILE_NAME}_${externalFileDate}.json"), null) { path, uri ->
            // 파일이 스캔된 후 콜백에서 결과를 처리합니다.
            if (uri != null) {
                Log.d(TAG, "saveTextToFile: ${path} ${uri}")// 파일이 성공적으로 스캔되었을 때 URI 반환
                uri.let {
                    Log.d(TAG, "findClickFile: ${resolver}")
                    resolver.delete(it, null, null)
                    saveClickFile(content, externalFileDate)
                }
            } else {
                Log.d(TAG, "saveTextToFile: ")
                saveClickFile(content, externalFileDate)// 스캔 실패 시 null 반환
            }
        }
    }

    private fun saveClickFile(content: String, externalFileDate: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${CLICKER000_EXTERNAL_FILE_NAME}_${externalFileDate}")
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
}