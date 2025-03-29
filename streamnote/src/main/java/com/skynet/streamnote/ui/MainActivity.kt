package com.skynet.streamnote.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.skynet.streamnote.ui.screens.StreamNoteApp
import com.skynet.streamnote.ui.theme.StreamNoteTheme
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StreamNoteViewModel by viewModels()

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 권한 결과 처리
        if (viewModel.hasOverlayPermission()) {
            // 권한이 부여된 경우 서비스 시작 가능
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StreamNoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StreamNoteApp(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 앱이 다시 포그라운드로 왔을 때 오버레이 권한 확인
        if (!viewModel.hasOverlayPermission()) {
            requestOverlayPermission()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }
}