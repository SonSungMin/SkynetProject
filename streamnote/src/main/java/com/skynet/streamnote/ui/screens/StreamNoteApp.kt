package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamNoteApp(viewModel: StreamNoteViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("메모 목록", "테마 설정")
    val serviceRunning = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StreamNote") },
                actions = {
                    Switch(
                        checked = serviceRunning.value,
                        onCheckedChange = { isChecked ->
                            serviceRunning.value = isChecked
                            if (isChecked) {
                                viewModel.startOverlayService()
                            } else {
                                viewModel.stopOverlayService()
                            }
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (index == 0) Icons.Default.List else Icons.Default.Settings,
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> MemoListScreen(viewModel, Modifier.padding(innerPadding))
            1 -> ThemeSettingsScreen(viewModel, Modifier.padding(innerPadding))
        }
    }
}