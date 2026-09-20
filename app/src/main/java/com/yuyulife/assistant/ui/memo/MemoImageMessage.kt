package com.yuyulife.assistant.ui.memo

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MemoImageMessage(
    file: File,
    onOpen: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bitmap by rememberMemoImage(file, maxDimension = 720)
    Box(
        modifier = modifier
            .width(220.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onOpen, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap == null) {
            Text("图片无法预览", color = MaterialTheme.colorScheme.onPrimaryContainer)
        } else {
            Image(
                bitmap = bitmap!!,
                contentDescription = "备忘录图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
