package com.example.clicker.model.data.youtube

data class VideoList(
    val etag: String,
    val items: List<Item>,
    val kind: String,
    val pageInfo: PageInfo
)