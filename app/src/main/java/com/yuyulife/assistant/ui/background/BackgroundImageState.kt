package com.yuyulife.assistant.ui.background

import androidx.compose.ui.graphics.ImageBitmap

sealed interface BackgroundImageState {
    data object Disabled : BackgroundImageState
    data object Loading : BackgroundImageState
    data class Ready(val bitmap: ImageBitmap) : BackgroundImageState
    data object Error : BackgroundImageState
}
