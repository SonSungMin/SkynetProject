package com.skynet.skytimelock.bean

import android.graphics.drawable.Drawable

/**
 * 프로그램 목록 정보를 담는 데이터 클래스
 */
data class SkyProgListInfo(
    var APP_ICON: Drawable? = null,
    var APP_NAME: String = "",
    var APP_PKG_NAME: String = "",
    var APP_GRAPH: Long = 0,
    var APP_RATE: Float = 0f,
    var APP_USE_TIME: String = "",
    /**
     * 정렬 순서
     */
    var APP_INDEX: Int = 0,
    /**
     * 실행 횟수
     */
    var APP_RUN: Int = 0
)