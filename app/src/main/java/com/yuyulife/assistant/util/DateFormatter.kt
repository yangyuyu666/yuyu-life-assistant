package com.yuyulife.assistant.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String = SimpleDateFormat(
    "MM月dd日 HH:mm",
    Locale.CHINA,
).format(Date(timestamp))
