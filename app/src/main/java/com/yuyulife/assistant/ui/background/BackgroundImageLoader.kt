package com.yuyulife.assistant.ui.background

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberBackgroundImage(
    enabled: Boolean,
    uri: String?,
): State<BackgroundImageState> {
    val context = LocalContext.current.applicationContext
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val targetWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val targetHeight = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    return produceState<BackgroundImageState>(
        if (enabled) BackgroundImageState.Loading else BackgroundImageState.Disabled,
        enabled,
        uri,
        targetWidth,
        targetHeight,
    ) {
        value = when {
            !enabled -> BackgroundImageState.Disabled
            uri.isNullOrBlank() -> BackgroundImageState.Error
            else -> withContext(Dispatchers.IO) {
                loadSampledBitmap(context, Uri.parse(uri), targetWidth, targetHeight)
                    ?.let { BackgroundImageState.Ready(it.asImageBitmap()) }
                    ?: BackgroundImageState.Error
            }
        }
    }
}

private fun loadSampledBitmap(
    context: Context,
    uri: Uri,
    targetWidth: Int,
    targetHeight: Int,
): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            null
        } else {
            var sampleSize = 1
            while (
                bounds.outWidth / (sampleSize * 2) >= targetWidth &&
                bounds.outHeight / (sampleSize * 2) >= targetHeight
            ) {
                sampleSize *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }
    } catch (_: Exception) {
        null
    }
}
