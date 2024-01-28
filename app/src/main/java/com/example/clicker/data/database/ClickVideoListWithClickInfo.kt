package com.example.clicker.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class ClickVideoListWithClickInfo(
    @Embedded val clickVideoList : ClickVideoList,
    @Relation(
        parentColumn = "postId",
        entityColumn = "postId"
    )

    val clickInfoList : List<ClickVideoList>
)
