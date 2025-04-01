package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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

    // 기능별 색상 정의
    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val surfaceVariantColor = colorResource(id = R.color.surface_variant)
    val onPrimaryColor = colorResource(id = R.color.on_primary)
    val disabledColor = colorResource(id = R.color.disabled)
    val backgroundColor = colorResource(id = R.color.background)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), color = onPrimaryColor) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                ),
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
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = onPrimaryColor,
                            checkedTrackColor = surfaceVariantColor,
                            uncheckedThumbColor = onPrimaryColor,
                            uncheckedTrackColor = disabledColor
                        )
                    )
                }
            )
        },
        bottomBar = {
            Column {
                // 배너 광고 추가
                BannerAdView()

                // 기존 탭바
                NavigationBar(
                    containerColor = backgroundColor
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (index == 0) Icons.Default.List else Icons.Default.Settings,
                                    contentDescription = title,
                                    tint = if (selectedTab == index) primaryColor else disabledColor
                                )
                            },
                            label = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) primaryColor else disabledColor
                                )
                            },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        when (selectedTab) {
            0 -> MemoListScreen(viewModel, Modifier.padding(innerPadding))
            1 -> ThemeSettingsScreen(viewModel, Modifier.padding(innerPadding))
        }
    }
}