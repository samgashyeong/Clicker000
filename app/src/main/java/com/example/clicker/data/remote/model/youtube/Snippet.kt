package com.example.clicker.data.remote.model.youtube

import java.io.Serializable

data class Snippet(
    val categoryId: String,
    val channelId: String,
    val channelTitle: String,
    val defaultAudioLanguage: String,
    val description: String,
    val liveBroadcastContent: String,
    val localized: Localized,
    val publishedAt: String,
    val thumbnails: Thumbnails,
    val title: String
): Serializable