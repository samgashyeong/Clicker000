package com.example.clicker.viewmodel.main.model

import com.example.clicker.util.Mode
import javax.annotation.concurrent.Immutable

@Immutable
data class SettingUiModel(
    val isChangeButton : Boolean = false,
    val isVidButton : Boolean = false,
    val setStartPoint : Boolean = false,
    val mode : Mode = Mode.Default(),
    val externalFileDate : String = ""
)