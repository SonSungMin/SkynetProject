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
        // 현재 테마 수를 확인
        val currentThemes = themeDao.getAllThemes().first()

        // 이미 테마가 있으면 종료
        if (currentThemes.isNotEmpty()) {
            return
        }

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
            ),
            Theme(
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
            ),
            Theme(
                name = "형광 그린",
                backgroundColor = Color.argb(160, 0, 0, 0),
                textColor = Color.argb(255, 0, 255, 0),
                textSize = 18f,
                fontFamily = "Monospace",
                isBold = false,
                isItalic = false,
                scrollSpeed = 1.2f,
                position = "TOP",
                marginTop = 5,
                marginBottom = 0,
                marginHorizontal = 0
            ),
            Theme(
                name = "깔끔한 화이트",
                backgroundColor = Color.argb(180, 255, 255, 255),
                textColor = Color.BLACK,
                textSize = 16f,
                fontFamily = "Serif",
                isBold = false,
                isItalic = false,
                scrollSpeed = 1.0f,
                position = "BOTTOM",
                marginTop = 0,
                marginBottom = 10,
                marginHorizontal = 10
            )
        )

        for (theme in themes) {
            val id = insertTheme(theme)
        }
    }

    // 테마 업데이트 함수 추가
    suspend fun updateTheme(theme: Theme) {

        themeDao.updateTheme(theme)
        // 저장 후 테마 다시 가져와서 로그로 확인
        val updatedTheme = themeDao.getThemeById(theme.id).first()
    }
}