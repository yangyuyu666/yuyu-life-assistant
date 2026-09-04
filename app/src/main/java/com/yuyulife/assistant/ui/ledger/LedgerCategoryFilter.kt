package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.LedgerCategory

@Composable
fun LedgerCategoryFilter(
    categories: List<LedgerCategory>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("全部分类") },
            )
        }
        items(categories, key = LedgerCategory::id) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text("${category.type.displayName} · ${category.name}") },
            )
        }
    }
}
