package com.skynet.streamnote.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * 앱이 필요로 하는 권한 처리를 위한 유틸리티 클래스
 */
object PermissionUtil {

    // 캘린더 읽기 및 쓰기 권한
    val CALENDAR_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    // 계정 접근 권한 (Android 8.0 이하에서 필요)
    val ACCOUNT_PERMISSIONS = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        arrayOf(Manifest.permission.GET_ACCOUNTS)
    } else {
        emptyArray()
    }

    /**
     * 필요한 모든 권한이 부여되었는지 확인합니다.
     *
     * @param context 컨텍스트
     * @param permissions 확인할 권한 배열
     * @return 모든 권한이 부여되었으면 true, 아니면 false
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 캘린더 관련 권한이 있는지 확인합니다.
     *
     * @param context 컨텍스트
     * @return 캘린더 권한이 있으면 true, 아니면 false
     */
    fun hasCalendarPermissions(context: Context): Boolean {
        return hasPermissions(context, CALENDAR_PERMISSIONS)
    }

    /**
     * 계정 접근 권한이 있는지 확인합니다 (Android 8.0 이하에서만 필요).
     *
     * @param context 컨텍스트
     * @return 계정 접근 권한이 있으면 true, 아니면 false
     */
    fun hasAccountPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            hasPermissions(context, ACCOUNT_PERMISSIONS)
        } else {
            true
        }
    }

    /**
     * 모든 필요한 권한을 요청합니다.
     *
     * @param permissionLauncher 권한 요청 런처
     */
    fun requestAllPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        val allPermissions = CALENDAR_PERMISSIONS + ACCOUNT_PERMISSIONS
        permissionLauncher.launch(allPermissions)
    }

    /**
     * 캘린더 권한을 요청합니다.
     *
     * @param permissionLauncher 권한 요청 런처
     */
    fun requestCalendarPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher.launch(CALENDAR_PERMISSIONS)
    }
}