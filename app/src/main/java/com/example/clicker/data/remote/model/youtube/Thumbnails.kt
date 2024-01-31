package com.example.clicker.data.remote.model.youtube

import java.io.Serializable

data class Thumbnails(
    val default: Default,
    val high: High,
    val medium: Medium
): Serializable