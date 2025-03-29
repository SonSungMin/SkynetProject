package com.skynet.streamnote.data.dao

import androidx.room.*
import com.skynet.streamnote.data.entity.Theme
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes ORDER BY id ASC")
    fun getAllThemes(): Flow<List<Theme>>

    @Query("SELECT * FROM themes WHERE id = :themeId")
    fun getThemeById(themeId: Int): Flow<Theme?>

    @Insert
    suspend fun insertTheme(theme: Theme): Long

    @Update
    suspend fun updateTheme(theme: Theme)
}