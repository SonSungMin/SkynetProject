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
    onSave: (Memo) -> Unit
) {
    val isNewMemo = memo == null
    val content = remember { mutableStateOf(memo?.content ?: "") }
    val isActive = remember { mutableStateOf(memo?.isActive ?: true) }
    val hasDateRange = remember { mutableStateOf(memo?.startDate != null) }

    // 날짜 포맷터
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // 시작 날짜와 종료 날짜를 String으로 변환하여 상태로 관리
    val startDateText = remember {
        mutableStateOf(
            memo?.startDate?.let { dateFormatter.format(Date(it)) } ?:
            dateFormatter.format(Date(System.currentTimeMillis()))
        )
    }
    val endDateText = remember {
        mutableStateOf(
            memo?.endDate?.let { dateFormatter.format(Date(it)) } ?:
            dateFormatter.format(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        )
    }

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
                    // 날짜 선택 UI
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("시작 날짜:")
                        OutlinedTextField(
                            value = startDateText.value,
                            onValueChange = { startDateText.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("종료 날짜:")
                        OutlinedTextField(
                            value = endDateText.value,
                            onValueChange = { endDateText.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd") }
                        )
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
                            // 날짜 문자열을 Long 타입의 타임스탬프로 변환
                            val startDateTimestamp = try {
                                if (hasDateRange.value) dateFormatter.parse(startDateText.value)?.time else null
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }

                            val endDateTimestamp = try {
                                if (hasDateRange.value) dateFormatter.parse(endDateText.value)?.time else null
                            } catch (e: Exception) {
                                System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                            }

                            val newMemo = Memo(
                                id = memo?.id ?: 0,
                                content = content.value,
                                isActive = isActive.value,
                                startDate = startDateTimestamp,
                                endDate = endDateTimestamp,
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