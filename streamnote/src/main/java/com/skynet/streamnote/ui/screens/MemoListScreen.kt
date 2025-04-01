package com.skynet.streamnote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.ui.components.MemoDialog
import com.skynet.streamnote.ui.components.MemoItem
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

@Composable
fun MemoListScreen(viewModel: StreamNoteViewModel, modifier: Modifier = Modifier) {
    val memos by viewModel.allMemos.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemo by remember { mutableStateOf<Memo?>(null) }

    // 기능별 색상 정의
    val primaryColor = colorResource(id = R.color.primary)
    val backgroundColor = colorResource(id = R.color.background)
    val onBackgroundColor = colorResource(id = R.color.on_background)
    val onPrimaryColor = colorResource(id = R.color.on_primary)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (memos.isEmpty()) {
            Text(
                text = stringResource(R.string.no_memos),
                modifier = Modifier.align(Alignment.Center),
                color = onBackgroundColor
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
                .padding(16.dp),
            containerColor = primaryColor,
            contentColor = onPrimaryColor
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