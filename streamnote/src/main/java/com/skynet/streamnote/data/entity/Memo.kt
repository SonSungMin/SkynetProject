package com.skynet.streamnote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isActive: Boolean = true,
    val startDate: Long? = null,
    val endDate: Long? = null,
    // themeId 필드 제거
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 