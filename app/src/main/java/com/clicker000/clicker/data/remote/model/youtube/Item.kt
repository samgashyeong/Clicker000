package com.clicker000.clicker.data.remote.model.youtube

import java.io.Serializable

data class Item(
    val etag: String,
    val id: String,
    val kind: String,
    val snippet: Snippet
): Serializable