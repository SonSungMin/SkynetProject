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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.colorResource
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamNoteApp(viewModel: StreamNoteViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.memo_list),
        stringResource(R.string.theme_settings)
    )
    val serviceRunning = remember { mutableStateOf(false) }

    // 색상 정의 - 모던한 스타일로 변경
    val selectedContainerColor = colorResource(id = R.color.primary)
    val selectedIconColor = colorResource(id = R.color.on_primary)
    val unselectedIconColor = colorResource(id = R.color.on_primary).copy(alpha = 0.7f)

    Scaffold(
        topBar = {
            // 세련된 스타일의 TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorResource(id = R.color.on_primary),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary)
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
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(id = R.color.on_primary),
                            checkedTrackColor = colorResource(id = R.color.accent),
                            uncheckedThumbColor = colorResource(id = R.color.on_primary),
                            uncheckedTrackColor = colorResource(id = R.color.secondary).copy(alpha = 0.5f)
                        )
                    )
                }
            )
        },
        bottomBar = {
            Column {
                // 배너 광고 추가
                BannerAdView()

                // 세련된 네비게이션 바 - 높이 줄이고 색상 대비 개선
                NavigationBar(
                    containerColor = colorResource(id = R.color.primary),
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp) // 높이 줄임
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (index == 0) Icons.Default.List else Icons.Default.Settings,
                                    contentDescription = title
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall, // 더 작은 폰트
                                    fontWeight = if(selectedTab == index) FontWeight.Medium else FontWeight.Normal
                                )
                            },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White, // 흰색으로 변경하여 대비 향상
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                selectedTextColor = Color.White, // 흰색으로 변경하여 대비 향상
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = colorResource(id = R.color.accent) // 지시자 색상 변경
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