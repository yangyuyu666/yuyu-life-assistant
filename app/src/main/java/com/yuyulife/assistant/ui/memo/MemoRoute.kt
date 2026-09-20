package com.yuyulife.assistant.ui.memo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuyulife.assistant.data.repository.MemoRepository
import com.yuyulife.assistant.domain.model.MemoMessage
import kotlinx.coroutines.launch

@Composable
fun MemoRoute(
    repository: MemoRepository,
    selectedThreadId: Long?,
    onSelectThread: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedThreadId == null) {
        MemoListRoute(
            repository = repository,
            onOpenThread = { onSelectThread(it) },
            modifier = modifier,
        )
    } else {
        MemoChatRoute(
            repository = repository,
            threadId = selectedThreadId,
            onBack = { onSelectThread(null) },
            modifier = modifier,
        )
    }
}

@Composable
private fun MemoListRoute(
    repository: MemoRepository,
    onOpenThread: (Long) -> Unit,
    modifier: Modifier,
) {
    val factory = remember(repository) { MemoListViewModel.factory(repository) }
    val viewModel: MemoListViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MemoListEvent.OpenThread -> onOpenThread(event.threadId)
                is MemoListEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MemoListScreen(
            uiState = uiState,
            onCreate = viewModel::createThread,
            onOpen = onOpenThread,
            onDelete = viewModel::deleteThread,
        )
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MemoChatRoute(
    repository: MemoRepository,
    threadId: Long,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val factory = remember(repository, threadId) { MemoChatViewModel.factory(repository, threadId) }
    val viewModel: MemoChatViewModel = viewModel(key = "memo-chat-$threadId", factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        viewModel.importFiles(it)
    }

    BackHandler(onBack = onBack)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MemoChatEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }
    LaunchedEffect(uiState.isLoaded, uiState.thread) {
        if (uiState.isLoaded && uiState.thread == null) onBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        MemoChatScreen(
            uiState = uiState,
            resolveAttachment = repository::resolveAttachment,
            onBack = onBack,
            onRename = viewModel::renameThread,
            onSendText = viewModel::sendText,
            onChooseFiles = { filePicker.launch(arrayOf("*/*")) },
            onDeleteMessage = viewModel::deleteMessage,
            onOpenAttachment = { message ->
                if (!openMemoAttachment(context, repository, message)) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("文件不存在或没有可打开它的应用")
                    }
                }
            },
        )
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun openMemoAttachment(
    context: Context,
    repository: MemoRepository,
    message: MemoMessage,
): Boolean {
    val uri = repository.attachmentContentUri(message.attachmentPath) ?: return false
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, message.attachmentMimeType ?: "application/octet-stream")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (intent.resolveActivity(context.packageManager) == null) return false
    return try {
        context.startActivity(Intent.createChooser(intent, "打开文件"))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
