package com.yuyulife.assistant.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.ui.ledger.LedgerRoute
import com.yuyulife.assistant.ui.cover.AppCoverScreen
import com.yuyulife.assistant.ui.theme.YuyuLifeTheme
import com.yuyulife.assistant.ui.todo.TodoRoute
import com.yuyulife.assistant.ui.settings.SettingsRoute
import com.yuyulife.assistant.ui.background.BackgroundImageState
import com.yuyulife.assistant.ui.background.rememberBackgroundImage

@Composable
fun YuyuLifeApp(
    todoRepository: TodoRepository,
    ledgerRepository: LedgerRepository,
    settingsRepository: SettingsRepository,
) {
    YuyuLifeTheme {
        var showCover by rememberSaveable { mutableStateOf(true) }
        var currentSection by rememberSaveable { mutableStateOf(AppSection.TODO) }
        val settings by settingsRepository.settings.collectAsStateWithLifecycle()
        val backgroundRequested = settings.customBackgroundEnabled &&
            currentSection != AppSection.SETTINGS
        val backgroundState by rememberBackgroundImage(
            enabled = backgroundRequested,
            uri = settings.customBackgroundUri,
        )
        val snackbarHostState = remember { SnackbarHostState() }

        if (showCover) {
            AppCoverScreen(onEnter = { showCover = false })
            return@YuyuLifeTheme
        }

        LaunchedEffect(backgroundRequested, backgroundState) {
            if (backgroundRequested && backgroundState == BackgroundImageState.Error) {
                val result = snackbarHostState.showSnackbar(
                    message = "背景图片无法读取，请重新选择",
                    actionLabel = "去设置",
                )
                if (result == SnackbarResult.ActionPerformed) {
                    currentSection = AppSection.SETTINGS
                }
            }
        }

        val readyBackground = backgroundState as? BackgroundImageState.Ready
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            readyBackground?.let {
                Image(
                    bitmap = it.bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)),
                )
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (readyBackground == null) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar {
                        AppSection.entries.forEach { section ->
                            NavigationBarItem(
                                selected = currentSection == section,
                                onClick = { currentSection = section },
                                icon = { Text(section.symbol) },
                                label = { Text(section.label) },
                            )
                        }
                    }
                },
            ) { contentPadding ->
                when (currentSection) {
                    AppSection.TODO -> TodoRoute(
                        repository = todoRepository,
                        modifier = Modifier.padding(contentPadding),
                    )

                    AppSection.LEDGER -> LedgerRoute(
                        repository = ledgerRepository,
                        modifier = Modifier.padding(contentPadding),
                    )

                    AppSection.SETTINGS -> SettingsRoute(
                        settingsRepository = settingsRepository,
                        todoRepository = todoRepository,
                        modifier = Modifier.padding(contentPadding),
                    )
                }
            }
        }
    }
}
