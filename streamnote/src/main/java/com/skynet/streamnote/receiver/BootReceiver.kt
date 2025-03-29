package com.skynet.streamnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.skynet.streamnote.service.MemoOverlayService

/**
 * 부팅 완료 후 오버레이 서비스를 자동으로 시작하는 리시버
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPrefs = context.getSharedPreferences("streamnote_preferences", Context.MODE_PRIVATE)
            val serviceEnabled = sharedPrefs.getBoolean("overlay_service_enabled", false)

            // 설정에서 서비스 자동 실행이 활성화되어 있는 경우만 실행
            if (serviceEnabled && Settings.canDrawOverlays(context)) {
                val serviceIntent = Intent(context, MemoOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}