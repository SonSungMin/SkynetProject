package com.skynet.db

import android.provider.BaseColumns

class DbInitParam private constructor() {

    class DbBaseParam private constructor() : BaseColumns {
        companion object {
            const val DB_NAME = "skytimelock"
            const val DB_VERSION = 90
        }
    }

    companion object {
        // 필요한 경우 여기에 상수 추가
    }
}