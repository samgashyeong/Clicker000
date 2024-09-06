package com.example.clicker.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ApiKeyProvider {
    fun getApiKey() : String
}
class ApiKeyProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiKeyProvider {
    override fun getApiKey() : String {
        val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
        return ai.metaData?.getString("youtubeApi")!!
    }
}
