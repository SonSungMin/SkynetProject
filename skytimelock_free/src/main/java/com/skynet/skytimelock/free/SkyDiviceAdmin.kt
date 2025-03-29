package com.skynet.skytimelock.free

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class SkyDiviceAdmin : DeviceAdminReceiver() {

    companion object {
        fun getSamplePreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(SkyDiviceAdmin::class.java.name, 0)
        }
    }

    private fun showToast(context: Context, msg: CharSequence) {
        // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context, intent: Intent) {
        // 기기권한이 설정되었을 경우
        showToast(context, "Device Admin enabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "This is an optional message to warn the user about disabling."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        // 기기 권한이 해제 됬을 경우
        showToast(context, "Device Admin Disabled")
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        // 패스워드 상태가 변경되었을 경우
        showToast(context, "Password Change")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        // 패스워드 입력이 실패했을 겨우
        showToast(context, "PasswordFailed")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        // 패스워드를 정상적으로 입력했을 경우
        showToast(context, "PasswordSucceeded")
    }
}