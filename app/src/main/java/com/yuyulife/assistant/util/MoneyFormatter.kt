package com.yuyulife.assistant.util

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun parseAmountToCents(input: String): Long? {
    val normalized = input.trim().replace(",", "")
    val amount = normalized.toBigDecimalOrNull() ?: return null
    if (amount <= java.math.BigDecimal.ZERO) return null

    return runCatching {
        amount
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()
}

fun formatCents(cents: Long): String = NumberFormat
    .getCurrencyInstance(Locale.CHINA)
    .format(cents / 100.0)

