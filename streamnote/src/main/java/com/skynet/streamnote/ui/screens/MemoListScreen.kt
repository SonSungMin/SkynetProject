package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.ui.components.MemoDialog
import com.skynet.streamnote.ui.components.MemoItem
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoListScreen(viewModel: StreamNoteViewModel, modifier: Modifier = Modifier) {
    val memos by viewModel.allMemos.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemo by remember { mutableStateOf<Memo?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (memos.isEmpty()) {
            Text(
                text = stringResource(R.string.no_memos),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn {
                items(memos) { memo ->
                    MemoItem(
                        memo = memo,
                        onToggleActive = {
                            val updatedMemo = memo.copy(isActive = !memo.isActive)
                            viewModel.updateMemo(updatedMemo)
                        },
                        onEditClick = {
                            selectedMemo = memo
                            showAddDialog = true
                        },
                        onDeleteClick = {
                            viewModel.deleteMemo(memo)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                selectedMemo = null
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_memo))
        }
    }

    if (showAddDialog) {
        MemoDialog(
            memo = selectedMemo,
            onDismiss = { showAddDialog = false },
            onSave = { memo ->
                if (selectedMemo == null) {
                    viewModel.insertMemo(memo)
                } else {
                    viewModel.updateMemo(memo)
                }
                showAddDialog = false
            }
        )
    }
}