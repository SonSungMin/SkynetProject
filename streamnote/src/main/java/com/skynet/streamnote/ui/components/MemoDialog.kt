package com.skynet.streamnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Memo
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

    // 색상 리소스 정의
    val primaryColor = colorResource(id = R.color.primary)
    val surfaceColor = colorResource(id = R.color.surface)
    val outlineColor = colorResource(id = R.color.outline)
    val disabledColor = colorResource(id = R.color.disabled)
    val onSurfaceColor = colorResource(id = R.color.on_surface)
    val onPrimaryColor = colorResource(id = R.color.on_primary)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(if (isNewMemo) R.string.new_memo else R.string.edit_memo),
                    style = MaterialTheme.typography.titleLarge,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content.value,
                    onValueChange = { content.value = it },
                    label = { Text(stringResource(R.string.memo_content), color = onSurfaceColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = outlineColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive.value,
                        onCheckedChange = { isActive.value = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            uncheckedColor = outlineColor
                        )
                    )
                    Text(stringResource(R.string.activate), color = onSurfaceColor)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasDateRange.value,
                        onCheckedChange = { hasDateRange.value = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            uncheckedColor = outlineColor
                        )
                    )
                    Text(stringResource(R.string.set_display_period), color = onSurfaceColor)
                }

                if (hasDateRange.value) {
                    // 날짜 선택 UI
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.start_date), color = onSurfaceColor)
                        OutlinedTextField(
                            value = startDateText.value,
                            onValueChange = { startDateText.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = outlineColor,
                                cursorColor = primaryColor
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(stringResource(R.string.end_date), color = onSurfaceColor)
                        OutlinedTextField(
                            value = endDateText.value,
                            onValueChange = { endDateText.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = outlineColor,
                                cursorColor = primaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
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
                        enabled = content.value.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor,
                            disabledContainerColor = disabledColor,
                            disabledContentColor = surfaceColor
                        )
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}