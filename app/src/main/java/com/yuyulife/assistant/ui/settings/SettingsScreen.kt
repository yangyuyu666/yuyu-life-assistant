package com.yuyulife.assistant.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.data.repository.LedgerCategoryRepository
import com.yuyulife.assistant.data.repository.CategoryChangeResult
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.domain.model.ReminderLeadTime
import com.yuyulife.assistant.domain.model.ReminderMode

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    todoRepository: TodoRepository,
    categoryRepository: LedgerCategoryRepository,
    modifier: Modifier = Modifier,
) {
    val factory = remember(settingsRepository, todoRepository, categoryRepository) {
        SettingsViewModel.factory(settingsRepository, todoRepository, categoryRepository)
    }
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onReminderLeadTimeSelected = viewModel::setTodoReminderLeadMinutes,
        onReminderModeSelected = viewModel::setReminderMode,
        onReminderPermissionReturned = viewModel::refreshReminderSchedule,
        onCustomBackgroundEnabledChange = viewModel::setCustomBackgroundEnabled,
        onCustomBackgroundSelected = viewModel::setCustomBackground,
        onAddCategory = viewModel::addCategory,
        onRenameCategory = viewModel::renameCategory,
        onDeleteCategory = viewModel::deleteCategory,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onReminderLeadTimeSelected: (Int) -> Unit,
    onReminderModeSelected: (ReminderMode) -> Unit,
    onReminderPermissionReturned: () -> Unit,
    onCustomBackgroundEnabledChange: (Boolean) -> Unit,
    onCustomBackgroundSelected: (String) -> Unit,
    onAddCategory: (TransactionType, String, (CategoryChangeResult) -> Unit) -> Unit,
    onRenameCategory: (Long, String, (CategoryChangeResult) -> Unit) -> Unit,
    onDeleteCategory: (Long, (CategoryChangeResult) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReminderDialog by remember { mutableStateOf(false) }
    var showReminderModeDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val selectedMinutes = uiState.settings.todoReminderLeadMinutes
    val context = LocalContext.current
    val backgroundUri = uiState.settings.customBackgroundUri
    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Some document providers already grant sufficient long-term access.
            }
            onCustomBackgroundSelected(it.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("设置", style = MaterialTheme.typography.headlineMedium)
            Text(
                "统一管理应用功能",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text("待办管理", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showReminderModeDialog = true },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("提醒方式", style = MaterialTheme.typography.titleMedium)
                    Text(
                        uiState.settings.reminderMode.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showReminderDialog = true },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("截止提醒", style = MaterialTheme.typography.titleMedium)
                    Text(
                        ReminderLeadTime.labelFor(selectedMinutes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }

        ReminderPermissionCard(
            mode = uiState.settings.reminderMode,
            onPermissionSettingsReturned = onReminderPermissionReturned,
        )

        Text("外观管理", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("自定义背景", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (backgroundUri == null) "从手机选择待办和账本背景" else "已选择背景图片",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Switch(
                        checked = uiState.settings.customBackgroundEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && backgroundUri == null) {
                                backgroundPicker.launch(arrayOf("image/*"))
                            } else {
                                onCustomBackgroundEnabledChange(enabled)
                            }
                        },
                    )
                }
                OutlinedButton(
                    onClick = { backgroundPicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (backgroundUri == null) "选择背景图片" else "更换背景图片")
                }
            }
        }

        Text("账本管理", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryDialog = true },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("收支分类", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "管理收入和支出的分类",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }

    if (showReminderDialog) {
        ReminderLeadTimeDialog(
            selectedMinutes = selectedMinutes,
            onSelect = onReminderLeadTimeSelected,
            onDismiss = { showReminderDialog = false },
        )
    }

    if (showReminderModeDialog) {
        ReminderModeDialog(
            selectedMode = uiState.settings.reminderMode,
            onSelect = onReminderModeSelected,
            onDismiss = { showReminderModeDialog = false },
        )
    }

    if (showCategoryDialog) {
        CategoryManagementDialog(
            categories = uiState.categories,
            onAdd = onAddCategory,
            onRename = onRenameCategory,
            onDelete = onDeleteCategory,
            onDismiss = { showCategoryDialog = false },
        )
    }
}
