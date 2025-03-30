package com.skynet.streamnote.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.skynet.streamnote.data.AppDatabase
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.ui.screens.StreamNoteApp
import com.skynet.streamnote.ui.theme.StreamNoteTheme
import com.skynet.streamnote.ui.viewmodel.StreamNoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        // 데이터베이스 초기화 및 기본 테마 추가
        val db = AppDatabase.getDatabase(this)
        val themeDao = db.themeDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val themeCount = themeDao.getAllThemes().first().size
            Log.d("MainActivity", "테마 개수: $themeCount")

            if (themeCount == 0) {
                // 기본 테마 직접 추가
                val theme1 = Theme(
                    name = "기본 검정",
                    backgroundColor = Color.argb(200, 0, 0, 0),
                    textColor = Color.WHITE,
                    textSize = 16f,
                    fontFamily = "Default",
                    isBold = false,
                    isItalic = false,
                    scrollSpeed = 1f,
                    position = "TOP",
                    marginTop = 0,
                    marginBottom = 0,
                    marginHorizontal = 0
                )

                val theme2 = Theme(
                    name = "스트리머 레드",
                    backgroundColor = Color.argb(180, 200, 0, 0),
                    textColor = Color.WHITE,
                    textSize = 18f,
                    fontFamily = "SansSerif",
                    isBold = true,
                    isItalic = false,
                    scrollSpeed = 1.3f,
                    position = "TOP",
                    marginTop = 10,
                    marginBottom = 0,
                    marginHorizontal = 5
                )

                val theme3 = Theme(
                    name = "게이머 블루",
                    backgroundColor = Color.argb(180, 0, 0, 180),
                    textColor = Color.WHITE,
                    textSize = 20f,
                    fontFamily = "Default",
                    isBold = true,
                    isItalic = false,
                    scrollSpeed = 1.5f,
                    position = "BOTTOM",
                    marginTop = 0,
                    marginBottom = 20,
                    marginHorizontal = 10
                )

                themeDao.insertTheme(theme1)
                themeDao.insertTheme(theme2)
                themeDao.insertTheme(theme3)

                Log.d("MainActivity", "기본 테마 3개 추가 완료")
            }
        }

        // AdMob 초기화
        MobileAds.initialize(this) {}

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