package com.skynet.streamnote.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.ui.components.ThemeEditDialog
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

@Composable
fun ThemeSettingsScreen(viewModel: StreamNoteViewModel, modifier: Modifier = Modifier) {
    val themes by viewModel.allThemes.collectAsState(initial = emptyList())
    val currentThemeId = remember { mutableStateOf(viewModel.getGlobalThemeId()) }
    var selectedTheme by remember { mutableStateOf<Theme?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // 로그 추가
    Log.d("ThemeSettingsScreen", "테마 개수: ${themes.size}")

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.select_theme),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        if (themes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.no_themes))

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // 수동으로 기본 테마 추가
                    viewModel.insertDefaultThemes()
                }) {
                    Text(stringResource(R.string.add_default_themes))
                }
            }
        } else {
            LazyColumn {
                items(themes) { theme ->
                    ThemeSettingItem(
                        theme = theme,
                        isSelected = theme.id == currentThemeId.value,
                        onClick = {
                            // 전역 테마로 선택
                            viewModel.saveGlobalThemeId(theme.id)
                            currentThemeId.value = theme.id
                        },
                        onEditClick = {
                            selectedTheme = theme
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog && selectedTheme != null) {
        ThemeEditDialog(
            theme = selectedTheme!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTheme ->
                viewModel.updateTheme(updatedTheme)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ThemeSettingItem(
    theme: Theme,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if(isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 테마 색상 미리보기
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(theme.backgroundColor))
            ) {
                val textStyle = when {
                    theme.isBold && theme.isItalic -> TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                    theme.isBold -> TextStyle(fontWeight = FontWeight.Bold)
                    theme.isItalic -> TextStyle(fontStyle = FontStyle.Italic)
                    else -> TextStyle()
                }

                val fontFamily = when (theme.fontFamily) {
                    "Serif" -> FontFamily.Serif
                    "Monospace" -> FontFamily.Monospace
                    "SansSerif" -> FontFamily.SansSerif
                    else -> FontFamily.Default
                }

                Text(
                    text = "Aa",
                    color = Color(theme.textColor),
                    fontSize = theme.textSize.sp,
                    fontFamily = fontFamily,
                    style = textStyle,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 테마 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium
                )

                val boldText = if(theme.isBold) stringResource(R.string.bold) else ""
                val italicText = if(theme.isItalic) stringResource(R.string.italic) else ""
                Text(
                    text = "${theme.fontFamily} $boldText $italicText".trim(),
                    style = MaterialTheme.typography.bodyMedium
                )

                val positionText = if(theme.position == "TOP")
                    stringResource(R.string.position_top_short)
                else
                    stringResource(R.string.position_bottom_short)

                Text(
                    text = stringResource(R.string.speed_position, theme.scrollSpeed, positionText),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 선택 표시
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            // 편집 버튼
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_theme),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}