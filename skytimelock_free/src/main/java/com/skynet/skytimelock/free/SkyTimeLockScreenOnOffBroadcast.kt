package com.skynet.skytimelock.free

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.skynet.common.SkyTimeLockCommon

class SkyTimeLockScreenOnOffBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val common = SkyTimeLockCommon(context)

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                // SkyPersistentService.sky_keyLock.reenableKeyguard()
                common.setScreenOn(false)
                common.setLogMsg("Screen Off")
            }
            Intent.ACTION_SCREEN_ON -> {
                // if(SkyPersistentService.setting_service.isOption2() && SkyPersistentService.sky_km.inKeyguardRestrictedInputMode())
                // {
                //     SkyPersistentService.sky_keyLock.disableKeyguard()
                // }
                common.setScreenOn(true)
                common.setLogMsg("Screen On")
            }
        }
    }
}