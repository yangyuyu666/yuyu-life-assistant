package com.yuyulife.assistant.ui.todo

internal fun toggleSelection(current: Set<Long>, itemId: Long): Set<Long> =
    current.toMutableSet().apply {
        if (!add(itemId)) remove(itemId)
    }
