package com.skynet.streamnote.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.skynet.streamnote.R
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.ui.components.ImportCalendarDialog
import com.skynet.streamnote.ui.components.MemoDialog
import com.skynet.streamnote.ui.components.MemoItem
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel
import androidx.compose.ui.res.colorResource

@Composable
fun MemoListScreen(viewModel: StreamNoteViewModel, modifier: Modifier = Modifier) {
    val memos by viewModel.allMemos.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemo by remember { mutableStateOf<Memo?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 캘린더 권한 요청을 위한 런처
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // 권한이 부여되면 가져오기 다이얼로그 표시
                showImportDialog = true
            } else {
                // 권한이 거부되면 권한 안내 다이얼로그 표시
                showPermissionDialog = true
            }
        }
    )

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

        // FAB들을 가로로 나란히 배치
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            // 캘린더 가져오기 FAB
            ExtendedFloatingActionButton(
                onClick = {
                    // 캘린더 권한 확인 후 처리
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CALENDAR
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showImportDialog = true
                    } else {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                text = { Text(stringResource(R.string.import_calendar)) }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 메모 추가 FAB - FloatingActionButton에서 ExtendedFloatingActionButton으로 변경
            ExtendedFloatingActionButton(
                onClick = {
                    selectedMemo = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_memo)) }
            )
        }
    }

    // 메모 추가/수정 다이얼로그
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

    // 캘린더 가져오기 다이얼로그
    if (showImportDialog) {
        ImportCalendarDialog(
            onDismiss = { showImportDialog = false },
            onImport = { daysRange ->
                viewModel.importCalendarEvents(daysRange)
                showImportDialog = false
            }
        )
    }

    // 권한 안내 다이얼로그
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.calendar_permission_explanation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                        showPermissionDialog = false
                    }
                ) {
                    Text(stringResource(R.string.request_again))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}