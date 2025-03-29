package com.skynet.skytimelock.free

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.skynet.common.SkyTimeLockCommon

class SkyTimeLockStartBroadcast : BroadcastReceiver() {
    private val ACTION = "action.service.SkyTimeLock"

    override fun onReceive(context: Context, intent: Intent) {
        val common = SkyTimeLockCommon(context)
        common.setRefreshSetting(true)
        common.setIsInit(true)

        if (common.getSetting("boot").isOption7() && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Intent i = new Intent(context, SkyPersistentService.class)
            context.startService(Intent(ACTION))
            Toast.makeText(context, "TimeGuide Started.", Toast.LENGTH_SHORT).show()
        }
    }
}