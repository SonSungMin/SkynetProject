package com.skynet.streamnote.util

import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.skynet.streamnote.data.entity.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 구글 캘린더 API 접근을 위한 유틸리티 클래스
 */
class GoogleCalendarUtil(private val context: Context) {

    /**
     * 지정된 기간 내의 캘린더 이벤트들을 가져옵니다.
     *
     * @param startTime 시작 시간 (밀리초)
     * @param endTime 종료 시간 (밀리초)
     * @return 캘린더 이벤트 목록
     */
    fun getCalendarEvents(startTime: Long, endTime: Long): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        // 가져올 캘린더 데이터의 컬럼들
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_ID
        )

        // 지정된 기간 내의 이벤트만 가져오는 선택 조건
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        // 시작 시간 기준으로 정렬
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        // 컨텐트 프로바이더를 통해 캘린더 데이터 쿼리
        val uri = CalendarContract.Events.CONTENT_URI
        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(CalendarContract.Events._ID)
            val titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE)
            val descriptionIndex = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val startTimeIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
            val endTimeIndex = cursor.getColumnIndex(CalendarContract.Events.DTEND)
            val locationIndex = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
            val calendarIdIndex = cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val title = cursor.getString(titleIndex) ?: ""
                val description = cursor.getString(descriptionIndex) ?: ""
                val eventStartTime = cursor.getLong(startTimeIndex)
                val eventEndTime = cursor.getLong(endTimeIndex)
                val location = cursor.getString(locationIndex) ?: ""
                val calendarId = cursor.getLong(calendarIdIndex)

                events.add(
                    CalendarEvent(
                        id = id,
                        title = title,
                        description = description,
                        startTime = eventStartTime,
                        endTime = eventEndTime,
                        location = location,
                        calendarId = calendarId
                    )
                )
            }
        }

        return events
    }

    /**
     * 특정 날짜의 캘린더 이벤트들을 가져옵니다.
     *
     * @param date 날짜
     * @return 캘린더 이벤트 목록
     */
    fun getEventsForDate(date: Date): List<CalendarEvent> {
        // 해당 날짜의 시작 시간(00:00:00)
        val startOfDay = clearTimeInfo(date)
        // 해당 날짜의 종료 시간(23:59:59) - 다음날 00:00:00 에서 1밀리초 빼기
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

        return getCalendarEvents(startOfDay, endOfDay)
    }

    /**
     * 매모를 캘린더에 추가합니다.
     *
     * @param title 이벤트 제목
     * @param description 이벤트 설명
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param location 위치 (선택적)
     * @return 추가된 이벤트의 URI 또는 null (추가 실패 시)
     */
    fun addEventToCalendar(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        location: String = ""
    ): Uri? {
        val values = android.content.ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, 1) // 기본 캘린더 ID(보통 1)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1) // 알림 설정
        }

        return try {
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 캘린더 이벤트를 메모 내용으로 변환합니다.
     *
     * @param event 캘린더 이벤트
     * @return 메모로 변환된 텍스트
     */
    fun formatEventAsMemo(event: CalendarEvent): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val startTime = dateFormat.format(Date(event.startTime))
        val endTime = dateFormat.format(Date(event.endTime))

        val sb = StringBuilder()
        sb.append("[일정] ${event.title}\n")
        sb.append("시간: $startTime ~ $endTime\n")

        if (event.location.isNotEmpty()) {
            sb.append("장소: ${event.location}\n")
        }

        if (event.description.isNotEmpty()) {
            sb.append("${event.description}")
        }

        return sb.toString()
    }

    /**
     * 날짜에서 시간 정보를 제거합니다(00:00:00으로 설정).
     *
     * @param date 날짜
     * @return 시간 정보가 제거된 날짜의 밀리초 값
     */
    private fun clearTimeInfo(date: Date): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 사용자의 모든 캘린더 목록을 가져옵니다.
     *
     * @return 캘린더 ID와 이름 쌍의 맵
     */
    fun getCalendarList(): Map<Long, String> {
        val calendarMap = mutableMapOf<Long, String>()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: "Unknown Calendar"
                calendarMap[id] = name
            }
        }

        return calendarMap
    }
}