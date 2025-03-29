package com.skynet.streamnote.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skynet.streamnote.data.AppDatabase
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.data.repository.MemoRepository
import com.skynet.streamnote.data.repository.ThemeRepository
import com.skynet.streamnote.service.MemoOverlayService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StreamNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val memoRepository: MemoRepository
    private val themeRepository: ThemeRepository

    val allMemos: Flow<List<Memo>>
    val allThemes: Flow<List<Theme>>

    init {
        val database = AppDatabase.getDatabase(application)
        memoRepository = MemoRepository(database.memoDao())
        themeRepository = ThemeRepository(database.themeDao())

        allMemos = memoRepository.getAllMemos()
        allThemes = themeRepository.getAllThemes()

        // 앱 초기 실행 시 기본 테마 생성
        viewModelScope.launch {
            if (allThemes.first().isEmpty()) {
                themeRepository.insertDefaultThemes()
            }
        }
    }

    fun getActiveMemos(): Flow<List<Memo>> = memoRepository.getActiveMemos()

    fun getThemeById(themeId: Int): Flow<Theme?> = themeRepository.getThemeById(themeId)

    fun insertMemo(memo: Memo) = viewModelScope.launch {
        memoRepository.insertMemo(memo)
    }

    fun updateMemo(memo: Memo) = viewModelScope.launch {
        memoRepository.updateMemo(memo)
    }

    fun deleteMemo(memo: Memo) = viewModelScope.launch {
        memoRepository.deleteMemo(memo)
    }

    fun updateTheme(theme: Theme) = viewModelScope.launch {
        themeRepository.updateTheme(theme)

        // 서비스에 테마 변경 알림
        val context = getApplication<Application>()
        val intent = Intent(context, MemoOverlayService::class.java).apply {
            action = MemoOverlayService.ACTION_UPDATE_THEME
            putExtra(MemoOverlayService.EXTRA_THEME_ID, theme.id)
        }
        context.startService(intent)
    }

    // 오버레이 서비스 시작/중지 메서드
    fun startOverlayService() {
        val context = getApplication<Application>()
        if (Settings.canDrawOverlays(context)) {
            val intent = Intent(context, MemoOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun stopOverlayService() {
        val context = getApplication<Application>()
        context.stopService(Intent(context, MemoOverlayService::class.java))
    }

    // 오버레이 권한 체크
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(getApplication())
    }
}