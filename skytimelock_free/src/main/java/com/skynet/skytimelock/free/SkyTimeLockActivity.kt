package com.skynet.skytimelock.free

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.skynet.db.DbManager
import com.skynet.skytimelock.view.SkyLockPatternView
import com.skynet.skytimelock.view.SkyLockView
import com.skynet.skytimelock.view.SkyTimeLockMainView

@SuppressLint("InlinedApi")
class SkyTimeLockActivity : Activity() {
    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting
    private val ACTION = "action.service.SkyTimeLock"

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        common = SkyTimeLockCommon(this)
        setting = common.getSetting("SkyTimeLockActivity")

        // DB 파일 복사
        val dbm = DbManager(this)
        //dbm.copyDbFile()
        //common.copyDbFile()

        val param = HashMap<Any, Any>().apply {
            put("pwd", "777")
            put("pattern", "")
        }

        dbm.executeUpdate("appsetting", param, null, null)

        common.setRefreshSetting(true)
        common.setIsInit(true)

        // 설정화면 잠금일 경우
        if (setting.isOption2()) {
            val i = if (setting.getMode() == "3") {
                // 패턴락
                Intent(this, SkyLockPatternView::class.java).apply {
                    putExtra(SkyLockPatternView._Mode, SkyLockPatternView.LPMode.ComparePattern)
                    putExtra(SkyLockPatternView._AutoSave, true)
                }
            } else {
                Intent(this, SkyLockView::class.java)
            }

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.putExtra("MODE", "START") // 처음 실행 : START, 실행 버전 : ACT, 암호설정 버전 : PWD
            i.putExtra("ISLOCK", false)
            i.putExtra("ISDELETED", false)
            i.putExtra("LMTTIME", 0)
            i.putExtra("PKGNME", "com.skynet.skytimelock.view.SkyTimeLockMainView")
            startActivity(i)
        } else {
            val intent = Intent(this, SkyTimeLockMainView::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        // 서비스 시작
        startService(Intent(ACTION))
        //sendBroadcast(new Intent("action.SkyTimeLockBroadcastReceiver"));

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}