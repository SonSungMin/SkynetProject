package com.skynet.streamnote.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skynet.streamnote.data.AppDatabase
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.data.repository.CalendarRepository
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

        // 앱 초기 실행 시 기본 테마 생성 - 비동기 로직 수정
        viewModelScope.launch {
            val themes = allThemes.first()
            if (themes.isEmpty()) {
                themeRepository.insertDefaultThemes()
                // 로그 추가
                val themesAfterInsert = themeRepository.getAllThemes().first()
            } else {
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

    // 전역 테마 ID 저장 함수
    fun saveGlobalThemeId(themeId: Int) {
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("streamnote_preferences", Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt("global_theme_id", themeId).apply()

        // 서비스에 테마 변경 알림
        val intent = Intent(context, MemoOverlayService::class.java).apply {
            action = MemoOverlayService.ACTION_UPDATE_THEME
            putExtra(MemoOverlayService.EXTRA_THEME_ID, themeId)
        }
        context.startService(intent)
    }

    // 저장된 전역 테마 ID 가져오기
    fun getGlobalThemeId(): Int {
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("streamnote_preferences", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("global_theme_id", 1) // 기본값 1 (첫 번째 테마)
    }

    // 기본 테마 수동 추가 함수
    fun insertDefaultThemes() = viewModelScope.launch {
        themeRepository.insertDefaultThemes()
    }

    private val calendarRepository: CalendarRepository = CalendarRepository(application)

    // 캘린더 이벤트를 메모로 가져오는 함수
    fun importCalendarEvents(daysRange: Int = 7) = viewModelScope.launch {
        val calendarMemos = calendarRepository.getCalendarEvents(daysRange)

        // 각 캘린더 이벤트를 메모로 저장
        for (memo in calendarMemos) {
            memoRepository.insertMemo(memo)
        }
    }

    // 캘린더 권한 상태 확인
    fun hasCalendarPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readCalendarPermission = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.READ_CALENDAR
            )
            readCalendarPermission == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}