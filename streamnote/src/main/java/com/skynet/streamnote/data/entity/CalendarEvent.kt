package com.skynet.streamnote.data.entity

/**
 * 구글 캘린더 이벤트 정보를 담는 데이터 클래스
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String,
    val calendarId: Long
)