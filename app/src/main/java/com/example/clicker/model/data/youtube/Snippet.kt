package com.example.clicker.model.data.youtube

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
)