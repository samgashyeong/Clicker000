package com.clicker000.clicker.data.remote.model.youtube

import java.io.Serializable

data class Thumbnails(
    val default: Default,
    val high: High,
    val medium: Medium
): Serializable