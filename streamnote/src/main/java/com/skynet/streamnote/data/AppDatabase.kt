package com.skynet.streamnote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.skynet.streamnote.data.dao.MemoDao
import com.skynet.streamnote.data.dao.ThemeDao
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Memo::class, Theme::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun themeDao(): ThemeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "streamnote_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 데이터베이스 초기화 메서드 추가
        fun resetDatabase(context: Context) {
            context.deleteDatabase("streamnote_database")
            INSTANCE = null

            // 데이터베이스 재생성 및 기본 테마 추가
            CoroutineScope(Dispatchers.IO).launch {
                val db = getDatabase(context)
                val themeDao = db.themeDao()

                // 기본 테마 추가
                val theme1 = Theme(
                    name = "기본 검정",
                    backgroundColor = android.graphics.Color.argb(200, 0, 0, 0),
                    textColor = android.graphics.Color.WHITE,
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
                    backgroundColor = android.graphics.Color.argb(180, 200, 0, 0),
                    textColor = android.graphics.Color.WHITE,
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
                    backgroundColor = android.graphics.Color.argb(180, 0, 0, 180),
                    textColor = android.graphics.Color.WHITE,
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
            }
        }
    }
}