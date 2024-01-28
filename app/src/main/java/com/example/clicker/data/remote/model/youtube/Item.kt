package com.example.clicker.data.remote.model.youtube

data class Item(
    val etag: String,
    val id: String,
    val kind: String,
    val snippet: Snippet
)