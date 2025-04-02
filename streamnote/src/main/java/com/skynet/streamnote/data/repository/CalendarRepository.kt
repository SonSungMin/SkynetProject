package com.skynet.streamnote.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.skynet.streamnote.data.entity.Memo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CalendarRepository(private val context: Context) {

    // 향후 n일 동안의 이벤트를 가져오는 기본값
    private val DEFAULT_DAYS_RANGE = 7

    /**
     * 구글 캘린더에서 이벤트를 가져와 메모 리스트로 변환
     * @param daysRange 오늘부터 앞으로 몇 일 동안의 이벤트를 가져올지 설정
     * @return 이벤트를 기반으로 생성된 메모 리스트
     */
    fun getCalendarEvents(daysRange: Int = DEFAULT_DAYS_RANGE): List<Memo> {
        val memos = mutableListOf<Memo>()

        // 캘린더 권한이 있는지 확인
        if (!hasCalendarPermission()) {
            return memos
        }

        // 현재 시간 가져오기
        val now = System.currentTimeMillis()

        // 쿼리할 기간 설정 (현재부터 daysRange일 후까지)
        val endTime = now + (daysRange * 24 * 60 * 60 * 1000L)

        // 쿼리할 컬럼 정의
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )

        // 캘린더 이벤트 필터링 (현재 시간부터 향후 daysRange일 동안)
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), endTime.toString())

        // 이벤트 시작 시간 기준으로 정렬
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        // ContentResolver를 사용하여 캘린더 이벤트 쿼리
        val uri = CalendarContract.Events.CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        // 데이터베이스 커서 처리
        cursor?.use {
            val idColumn = it.getColumnIndex(CalendarContract.Events._ID)
            val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
            val descriptionColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val startColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endColumn = it.getColumnIndex(CalendarContract.Events.DTEND)
            val locationColumn = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)

            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

            // 각 이벤트를 처리
            while (it.moveToNext()) {
                val id = if (idColumn != -1) it.getLong(idColumn) else 0
                val title = if (titleColumn != -1) it.getString(titleColumn) ?: "" else ""
                val description = if (descriptionColumn != -1) it.getString(descriptionColumn) ?: "" else ""
                val start = if (startColumn != -1) it.getLong(startColumn) else 0
                val end = if (endColumn != -1) it.getLong(endColumn) else 0
                val location = if (locationColumn != -1) it.getString(locationColumn) ?: "" else ""

                // 메모 내용 구성
                val startDate = dateFormat.format(Date(start))
                val endDate = dateFormat.format(Date(end))
                val content = buildString {
                    append("📅 $title")
                    append("\n⏰ $startDate - $endDate")
                    if (location.isNotEmpty()) {
                        append("\n📍 $location")
                    }
                    if (description.isNotEmpty()) {
                        append("\n📝 $description")
                    }
                }

                // 메모 생성
                val memo = Memo(
                    content = content,
                    isActive = true,
                    startDate = start,
                    endDate = end,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                memos.add(memo)
            }
        }

        return memos
    }

    /**
     * 캘린더 권한이 있는지 확인
     */
    private fun hasCalendarPermission(): Boolean {
        try {
            val uri: Uri = CalendarContract.Calendars.CONTENT_URI
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.close()
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}