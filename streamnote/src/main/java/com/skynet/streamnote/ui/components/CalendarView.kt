package com.skynet.streamnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DateInfo(val date: Date, val isCurrentMonth: Boolean)

@Composable
fun CalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onMonthChanged: (Calendar) -> Unit = {},
    events: List<Date> = emptyList()
) {
    val calendar = remember { Calendar.getInstance() }
    calendar.time = selectedDate

    val currentCalendar = remember { mutableStateOf(Calendar.getInstance().apply {
        time = selectedDate
        set(Calendar.DAY_OF_MONTH, 1) // 달의 첫날로 설정
    })}

    val dateFormatter = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth()) {
        // 달력 헤더 (년월 + 이전/다음 버튼)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentCalendar.value.add(Calendar.MONTH, -1)
                onMonthChanged(currentCalendar.value)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "이전 달"
                )
            }

            Text(
                text = dateFormatter.format(currentCalendar.value.time),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                currentCalendar.value.add(Calendar.MONTH, 1)
                onMonthChanged(currentCalendar.value)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "다음 달"
                )
            }
        }

        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (day == "일") Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 그리드
        val currentMonth = generateCalendarDays(currentCalendar.value)

        // 날짜를 7개씩 묶어서 행으로 표시
        for (weekIndex in 0 until (currentMonth.size + 6) / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val index = weekIndex * 7 + dayIndex
                    if (index < currentMonth.size) {
                        val dayInfo = currentMonth[index]

                        // 이벤트가 있는 날짜인지 확인
                        val hasEvent = events.any { eventDate ->
                            isSameDay(eventDate, dayInfo.date)
                        }

                        // 선택된 날짜인지 확인
                        val isSelected = isSameDay(selectedDate, dayInfo.date)

                        // 오늘 날짜인지 확인
                        val isToday = isSameDay(Calendar.getInstance().time, dayInfo.date)

                        DateCell(
                            date = dayInfo.date,
                            isCurrentMonth = dayInfo.isCurrentMonth,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEvent = hasEvent,
                            onDateClick = { onDateSelected(dayInfo.date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // 빈 셀
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DateCell(
    date: Date,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(enabled = isCurrentMonth) { onDateClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = when {
                    !isCurrentMonth -> Color.Gray.copy(alpha = 0.5f)
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    dayOfWeek == Calendar.SUNDAY -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // 이벤트 표시 (작은 점)
            if (hasEvent && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

// 현재 월의 날짜 및 전/다음 월의 일부 날짜를 포함한 리스트 생성
private fun generateCalendarDays(calendar: Calendar): List<DateInfo> {
    val days = mutableListOf<DateInfo>()

    // 현재 달의 첫 날
    val currentCalendar = calendar.clone() as Calendar
    currentCalendar.set(Calendar.DAY_OF_MONTH, 1)

    // 현재 달
    val currentMonth = currentCalendar.get(Calendar.MONTH)

    // 이전 달의 마지막 날들 추가
    val firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0(일) ~ 6(토)
    if (firstDayOfWeek > 0) {
        val prevCalendar = currentCalendar.clone() as Calendar
        prevCalendar.add(Calendar.MONTH, -1)
        val lastDayOfPrevMonth = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            prevCalendar.set(Calendar.DAY_OF_MONTH, lastDayOfPrevMonth - firstDayOfWeek + i + 1)
            days.add(DateInfo(prevCalendar.time, false))
        }
    }

    // 현재 달의 날짜들 추가
    val maxDayOfMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDayOfMonth) {
        currentCalendar.set(Calendar.DAY_OF_MONTH, i)
        days.add(DateInfo(currentCalendar.time, true))
    }

    // 다음 달의 시작 일자들 추가 (6주 달력을 만들기 위해 필요한 만큼)
    val nextCalendar = currentCalendar.clone() as Calendar
    nextCalendar.add(Calendar.MONTH, 1)
    nextCalendar.set(Calendar.DAY_OF_MONTH, 1)

    val remainingCells = 42 - days.size // 6주 x 7일 = 42셀
    for (i in 1..remainingCells) {
        nextCalendar.set(Calendar.DAY_OF_MONTH, i)
        days.add(DateInfo(nextCalendar.time, false))
    }

    return days
}

// 두 날짜가 같은 날인지 비교
fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}