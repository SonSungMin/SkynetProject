// com/skynet/streamnote/ui/screens/ThemeSettingsScreen.kt
package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.ui.components.ThemeEditDialog
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

@Composable
fun ThemeSettingsScreen(viewModel: StreamNoteViewModel, modifier: Modifier = Modifier) {
    val themes by viewModel.allThemes.collectAsState(initial = emptyList())
    var selectedTheme by remember { mutableStateOf<Theme?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "사용 가능한 테마",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        if (themes.isEmpty()) {
            Text(
                text = "사용 가능한 테마가 없습니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        } else {
            LazyColumn {
                items(themes) { theme ->
                    ThemeSettingItem(
                        theme = theme,
                        onClick = {
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
fun ThemeSettingItem(theme: Theme, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${theme.fontFamily} ${if(theme.isBold) "굵게" else ""} ${if(theme.isItalic) "기울임" else ""}".trim(),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "속도: ${theme.scrollSpeed}x · 위치: ${if(theme.position == "TOP") "상단" else "하단"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "여백(상/하/좌우): ${theme.marginTop}/${theme.marginBottom}/${theme.marginHorizontal}dp",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "테마 편집",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}