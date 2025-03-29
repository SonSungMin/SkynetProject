package com.skynet.skytimelock.free

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.skynet.common.SkyTimeLockCommon

class SkyTimeLockScreenStartBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val common = SkyTimeLockCommon(context)

            // 부팅 시 자동 시작
            if (common.getSetting("SkyTimeLockScreenStartBroadcast").isOption7()) {
                context.startService(Intent(context, SkyPersistentService::class.java))
            }
        }
    }
}