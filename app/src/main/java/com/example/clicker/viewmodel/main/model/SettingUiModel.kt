package com.example.clicker.viewmodel.main.model

import com.example.clicker.viewmodel.Mode

data class SettingUiModel(
    val isChangeButton : Boolean = false,
    val isVidButton : Boolean = false,
    val mode : Mode? = null
)