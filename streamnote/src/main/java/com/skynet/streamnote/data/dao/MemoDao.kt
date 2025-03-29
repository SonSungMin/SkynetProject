package com.skynet.streamnote.data.dao

import androidx.room.*
import com.skynet.streamnote.data.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY updatedAt DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE isActive = 1 AND (startDate IS NULL OR startDate <= :currentTime) AND (endDate IS NULL OR endDate >= :currentTime) ORDER BY updatedAt DESC")
    fun getActiveMemos(currentTime: Long = System.currentTimeMillis()): Flow<List<Memo>>

    @Insert
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Delete
    suspend fun deleteMemo(memo: Memo)
}