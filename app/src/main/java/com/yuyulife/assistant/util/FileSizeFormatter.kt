package com.yuyulife.assistant.util

import java.util.Locale

fun formatFileSize(sizeBytes: Long?): String {
    val bytes = sizeBytes ?: return "大小未知"
    if (bytes < 1024) return "$bytes B"
    val kilobytes = bytes / 1024.0
    if (kilobytes < 1024) return String.format(Locale.getDefault(), "%.1f KB", kilobytes)
    val megabytes = kilobytes / 1024.0
    if (megabytes < 1024) return String.format(Locale.getDefault(), "%.1f MB", megabytes)
    return String.format(Locale.getDefault(), "%.1f GB", megabytes / 1024.0)
}
