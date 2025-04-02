package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skynet.streamnote.R
import com.skynet.streamnote.ui.components.BannerAdView
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamNoteApp(viewModel: StreamNoteViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.memo_list),
        stringResource(R.string.theme_settings)
    )
    val serviceRunning = remember { mutableStateOf(false) }

    // 색상 정의
    val selectedContainerColor = Color(0xFF3F4862)
    val selectedIconColor = Color.White
    val unselectedIconColor = Color.White.copy(alpha = 0.7f)

    Scaffold(
        topBar = {
            // 높이가 줄어든 TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                },
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
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            )
        },
        bottomBar = {
            Column {
                // 배너 광고 추가
                BannerAdView()

                // 기존 탭바
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
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedIconColor,
                                unselectedIconColor = unselectedIconColor,
                                selectedTextColor = selectedIconColor,
                                unselectedTextColor = unselectedIconColor,
                                indicatorColor = selectedContainerColor
                            )
                        )
                    }
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