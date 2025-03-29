package com.skynet.streamnote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
data class Theme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val backgroundColor: Int,
    val textColor: Int,
    val textSize: Float,
    val fontFamily: String = "Default",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val scrollSpeed: Float,
    val position: String = "TOP", // 위치 설정 (TOP 또는 BOTTOM)
    val marginTop: Int = 0,       // 위쪽 여백 (dp)
    val marginBottom: Int = 0,    // 아래쪽 여백 (dp)
    val marginHorizontal: Int = 0 // 좌우 여백 (dp)
)