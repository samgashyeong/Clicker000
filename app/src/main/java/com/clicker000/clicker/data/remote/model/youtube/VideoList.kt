package com.clicker000.clicker.data.remote.model.youtube

data class VideoList(
    val etag: String,
    val items: List<Item>,
    val kind: String,
    val pageInfo: PageInfo
)