package com.skynet.skytimelock.bean

import java.sql.Date

/**
 * 앱 목록 설정 정보를 담는 데이터 클래스
 */
data class SkyAppListSettingInfo(
    var pkgnme: String = "",
    var app_stdte: Date? = null,
    var app_eddte: Date? = null,
    var exe_gbn: String = "",
    var exe_stdte: Date? = null,
    var exe_eddte: Date? = null,
    var lmttime: Long = 0
) {
    // lmttime의 setter에서 int를 받는 오버로드된 메소드 추가
    // 원래 Java 코드에서 lmttime의 setter가 int를 받았지만 프로퍼티는 long 타입이었음
    fun setLmttime(value: Int) {
        this.lmttime = value.toLong()
    }
}