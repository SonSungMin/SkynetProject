package com.skynet.streamnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoDialog(
    memo: Memo?,
    onDismiss: () -> Unit,
    onSave: (Memo) -> Unit,
    viewModel: StreamNoteViewModel
) {
    val themes by viewModel.allThemes.collectAsState(initial = emptyList())
    val isNewMemo = memo == null
    val content = remember { mutableStateOf(memo?.content ?: "") }
    val isActive = remember { mutableStateOf(memo?.isActive ?: true) }
    val hasDateRange = remember { mutableStateOf(memo?.startDate != null) }
    val startDate = remember { mutableStateOf(memo?.startDate ?: System.currentTimeMillis()) }
    val endDate = remember { mutableStateOf(memo?.endDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) }
    val selectedThemeId = remember { mutableStateOf(memo?.themeId ?: 0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isNewMemo) "새 메모" else "메모 수정",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content.value,
                    onValueChange = { content.value = it },
                    label = { Text("메모 내용") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive.value,
                        onCheckedChange = { isActive.value = it }
                    )
                    Text("활성화")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasDateRange.value,
                        onCheckedChange = { hasDateRange.value = it }
                    )
                    Text("표시 기간 설정")
                }

                if (hasDateRange.value) {
                    // 날짜 선택 UI (간략하게 구현)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
                            // 시작 날짜 선택 다이얼로그 표시
                            // (실제 구현 시 DatePickerDialog 사용)
                        }) {
                            Text("시작 날짜")
                        }

                        Button(onClick = {
                            // 종료 날짜 선택 다이얼로그 표시
                        }) {
                            Text("종료 날짜")
                        }
                    }

                    // 선택된 날짜 표시
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(
                        text = "${dateFormat.format(Date(startDate.value))} ~ ${dateFormat.format(Date(endDate.value))}"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 테마 선택
                if (themes.isNotEmpty()) {
                    Text("테마 선택", style = MaterialTheme.typography.bodyLarge)

                    LazyRow {
                        items(themes) { theme ->
                            ThemeItem(
                                theme = theme,
                                isSelected = theme.id == selectedThemeId.value,
                                onClick = { selectedThemeId.value = theme.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val newMemo = Memo(
                                id = memo?.id ?: 0,
                                content = content.value,
                                isActive = isActive.value,
                                startDate = if (hasDateRange.value) startDate.value else null,
                                endDate = if (hasDateRange.value) endDate.value else null,
                                themeId = selectedThemeId.value,
                                createdAt = memo?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(newMemo)
                        },
                        enabled = content.value.isNotEmpty()
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeItem(
    theme: Theme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .background(
                color = Color(theme.backgroundColor),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "Aa",
            color = Color(theme.textColor),
            fontSize = theme.textSize.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}