package com.example.clicker.data.remote.model.youtube

data class VideoList(
    val etag: String,
    val items: List<Item>,
    val kind: String,
    val pageInfo: PageInfo
)