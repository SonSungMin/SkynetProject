package com.skynet.streamnote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Memo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoItem(
    memo: Memo,
    onToggleActive: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = colorResource(id = R.color.primary)
    val outlineColor = colorResource(id = R.color.outline)
    val surfaceColor = colorResource(id = R.color.surface)
    val disabledColor = colorResource(id = R.color.disabled)
    val onSurfaceColor = colorResource(id = R.color.on_surface)
    val onSurfaceVariantColor = colorResource(id = R.color.on_surface_variant)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memo.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    Switch(
                        checked = memo.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = surfaceColor,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = surfaceColor,
                            uncheckedTrackColor = disabledColor
                        )
                    )

                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_memo),
                            tint = primaryColor
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = primaryColor
                        )
                    }
                }
            }

            // 메모의 활성 기간 표시
            memo.startDate?.let { start ->
                memo.endDate?.let { end ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(
                        text = stringResource(
                            R.string.active_period,
                            dateFormat.format(Date(start)),
                            dateFormat.format(Date(end))
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )
                }
            }
        }
    }
}