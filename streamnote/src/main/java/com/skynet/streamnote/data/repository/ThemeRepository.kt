package com.skynet.streamnote.data.repository

import android.graphics.Color
import com.skynet.streamnote.data.dao.ThemeDao
import com.skynet.streamnote.data.entity.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ThemeRepository(private val themeDao: ThemeDao) {
    fun getAllThemes(): Flow<List<Theme>> = themeDao.getAllThemes()

    fun getThemeById(themeId: Int): Flow<Theme?> = themeDao.getThemeById(themeId)

    suspend fun insertTheme(theme: Theme): Long = themeDao.insertTheme(theme)

    suspend fun insertDefaultThemes() {
        // 기본 테마들 추가
        val themes = listOf(
            Theme(
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
            ),
            Theme(
                name = "밝은 파랑",
                backgroundColor = Color.argb(200, 0, 0, 200),
                textColor = Color.WHITE,
                textSize = 18f,
                fontFamily = "Default",
                isBold = true,
                isItalic = false,
                scrollSpeed = 1.2f,
                position = "TOP",
                marginTop = 0,
                marginBottom = 0,
                marginHorizontal = 0
            ),
            Theme(
                name = "빨간색 강조",
                backgroundColor = Color.argb(200, 200, 0, 0),
                textColor = Color.WHITE,
                textSize = 20f,
                fontFamily = "Default",
                isBold = true,
                isItalic = true,
                scrollSpeed = 1.5f,
                position = "TOP",
                marginTop = 0,
                marginBottom = 0,
                marginHorizontal = 0
            ),
            Theme(
                name = "투명 회색",
                backgroundColor = Color.argb(150, 100, 100, 100),
                textColor = Color.WHITE,
                textSize = 16f,
                fontFamily = "Monospace",
                isBold = false,
                isItalic = false,
                scrollSpeed = 0.8f,
                position = "TOP",
                marginTop = 0,
                marginBottom = 0,
                marginHorizontal = 0
            ),
            Theme(
                name = "어둠 모드",
                backgroundColor = Color.argb(220, 20, 20, 20),
                textColor = Color.argb(255, 0, 255, 0), // 녹색 텍스트
                textSize = 18f,
                fontFamily = "Serif",
                isBold = false,
                isItalic = false,
                scrollSpeed = 1.0f,
                position = "TOP",
                marginTop = 0,
                marginBottom = 0,
                marginHorizontal = 0
            )
        )

        themes.forEach { theme ->
            insertTheme(theme)
        }
    }

    // 테마 업데이트 함수 추가
    suspend fun updateTheme(theme: Theme) {

        themeDao.updateTheme(theme)
        // 저장 후 테마 다시 가져와서 로그로 확인
        val updatedTheme = themeDao.getThemeById(theme.id).first()
    }
}