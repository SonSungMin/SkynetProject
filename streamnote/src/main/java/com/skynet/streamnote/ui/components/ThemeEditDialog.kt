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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeEditDialog(
    theme: Theme,
    onDismiss: () -> Unit,
    onSave: (Theme) -> Unit
) {
    val name = remember { mutableStateOf(theme.name) }
    val backgroundColor = remember { mutableStateOf(theme.backgroundColor) }
    val textColor = remember { mutableStateOf(theme.textColor) }
    val textSize = remember { mutableStateOf(theme.textSize) }
    val fontFamily = remember { mutableStateOf(theme.fontFamily) }
    val isBold = remember { mutableStateOf(theme.isBold) }
    val isItalic = remember { mutableStateOf(theme.isItalic) }
    val scrollSpeed = remember { mutableStateOf(theme.scrollSpeed) }

    val fontFamilyOptions = listOf("Default", "Serif", "SansSerif", "Monospace")
    var expandedFontFamily by remember { mutableStateOf(false) }

    val position = remember { mutableStateOf(theme.position) }
    val marginTop = remember { mutableStateOf(theme.marginTop) }
    val marginBottom = remember { mutableStateOf(theme.marginBottom) }
    val marginHorizontal = remember { mutableStateOf(theme.marginHorizontal) }

    val positionOptions = listOf("TOP", "BOTTOM")
    var expandedPosition by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.edit_theme),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text(stringResource(R.string.theme_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 텍스트 색상 선택 (간단한 구현)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.text_color), modifier = Modifier.width(120.dp))

                    // 미리 정의된 색상 옵션들
                    ColorOption(
                        color = Color.White,
                        isSelected = textColor.value == -1, // 0xFFFFFFFF (흰색)
                        onClick = { textColor.value = -1 }
                    )
                    ColorOption(
                        color = Color.Black,
                        isSelected = textColor.value == -16777216, // 0xFF000000 (검정색)
                        onClick = { textColor.value = -16777216 }
                    )
                    ColorOption(
                        color = Color.Red,
                        isSelected = textColor.value == -65536, // 0xFFFF0000 (빨간색)
                        onClick = { textColor.value = -65536 }
                    )
                    ColorOption(
                        color = Color.Green,
                        isSelected = textColor.value == -16711936, // 0xFF00FF00 (녹색)
                        onClick = { textColor.value = -16711936 }
                    )
                    ColorOption(
                        color = Color.Yellow,
                        isSelected = textColor.value == -256, // 0xFFFFFF00 (노란색)
                        onClick = { textColor.value = -256 }
                    )
                    ColorOption(
                        color = Color.Cyan,
                        isSelected = textColor.value == -16711681, // 0xFF00FFFF (청록색)
                        onClick = { textColor.value = -16711681 }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 폰트 크기 슬라이더
                Text(stringResource(R.string.font_size, textSize.value.toInt(), "sp"))
                Slider(
                    value = textSize.value,
                    onValueChange = { textSize.value = it },
                    valueRange = 12f..30f,
                    steps = 18,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 폰트 패밀리 선택
                ExposedDropdownMenuBox(
                    expanded = expandedFontFamily,
                    onExpandedChange = { expandedFontFamily = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = fontFamily.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.font_family)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFontFamily)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFontFamily,
                        onDismissRequest = { expandedFontFamily = false }
                    ) {
                        fontFamilyOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    fontFamily.value = option
                                    expandedFontFamily = false
                                },
                                text = { Text(option) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 볼드 및 이탤릭 체크박스
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isBold.value,
                        onCheckedChange = { isBold.value = it }
                    )
                    Text(stringResource(R.string.bold))

                    Spacer(modifier = Modifier.width(16.dp))

                    Checkbox(
                        checked = isItalic.value,
                        onCheckedChange = { isItalic.value = it }
                    )
                    Text(stringResource(R.string.italic))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 스크롤 속도 슬라이더
                Text(stringResource(R.string.scroll_speed, scrollSpeed.value))
                Slider(
                    value = scrollSpeed.value,
                    onValueChange = { scrollSpeed.value = it },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 미리보기
                Text(stringResource(R.string.preview), style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(backgroundColor.value))
                        .padding(8.dp)
                ) {
                    val previewTextStyle = when {
                        isBold.value && isItalic.value -> TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                        isBold.value -> TextStyle(fontWeight = FontWeight.Bold)
                        isItalic.value -> TextStyle(fontStyle = FontStyle.Italic)
                        else -> TextStyle()
                    }

                    val previewFontFamily = when (fontFamily.value) {
                        "Serif" -> FontFamily.Serif
                        "Monospace" -> FontFamily.Monospace
                        "SansSerif" -> FontFamily.SansSerif
                        else -> FontFamily.Default
                    }

                    Text(
                        text = stringResource(R.string.preview_text),
                        color = Color(textColor.value),
                        fontSize = textSize.value.sp,
                        fontFamily = previewFontFamily,
                        style = previewTextStyle,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 위치 선택 (위/아래)
                Text(stringResource(R.string.text_position), style = MaterialTheme.typography.bodyLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = position.value == "TOP",
                        onClick = { position.value = "TOP" }
                    )
                    Text(stringResource(R.string.position_top))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = position.value == "BOTTOM",
                        onClick = { position.value = "BOTTOM" }
                    )
                    Text(stringResource(R.string.position_bottom))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 여백 설정
                Text(stringResource(R.string.margin_settings), style = MaterialTheme.typography.bodyLarge)

                // 상단 여백
                Text(stringResource(R.string.top_margin, marginTop.value))
                Slider(
                    value = marginTop.value.toFloat(),
                    onValueChange = { marginTop.value = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )

                // 하단 여백
                Text(stringResource(R.string.bottom_margin, marginBottom.value))
                Slider(
                    value = marginBottom.value.toFloat(),
                    onValueChange = { marginBottom.value = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )

                // 좌우 여백
                Text(stringResource(R.string.horizontal_margin, marginHorizontal.value))
                Slider(
                    value = marginHorizontal.value.toFloat(),
                    onValueChange = { marginHorizontal.value = it.toInt() },
                    valueRange = 0f..50f,
                    steps = 50,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val updatedTheme = theme.copy(
                                name = name.value,
                                backgroundColor = backgroundColor.value,
                                textColor = textColor.value,
                                textSize = textSize.value,
                                fontFamily = fontFamily.value,
                                isBold = isBold.value,
                                isItalic = isItalic.value,
                                scrollSpeed = scrollSpeed.value,
                                position = position.value,
                                marginTop = marginTop.value,
                                marginBottom = marginBottom.value,
                                marginHorizontal = marginHorizontal.value
                            )
                            onSave(updatedTheme)
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}