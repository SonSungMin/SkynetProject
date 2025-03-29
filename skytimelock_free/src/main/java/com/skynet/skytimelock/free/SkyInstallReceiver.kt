package com.skynet.skytimelock.free

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import java.util.ArrayList
import java.util.HashMap

class SkyInstallReceiver : BroadcastReceiver() {

    companion object {
        const val logTag = "### SKY ###"
        const val PACKAGE_INSTALL = "android.intent.action.PACKAGE_INSTALL"
        const val PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED"
        const val PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED"
        const val PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PACKAGE_ADDED) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            val dbm = DbManager(context)
            val common = SkyTimeLockCommon(context)
            val setting = common.getSetting("SkyInstallReceiver.onReceive")

            if (setting.isOption9()) {
                try {
                    val db_select_list = dbm.executeSelect("SELECT * FROM applist", null)

                    var cnt = 0

                    if (db_select_list != null && db_select_list.isNotEmpty()) {
                        cnt = 0

                        for (j in 0 until db_select_list.size) {
                            if (db_select_list[j]["pkgnme"].toString().trim().equals(packageName, ignoreCase = true)) {
                                cnt++
                                break
                            }
                        }
                    }

                    if (cnt == 0) {
                        val values = HashMap<Any, Any>()

                        values["pkgnme"] = packageName
                        // 1: 시간 제한, 2:잠금
                        values["exe_gbn"] = "2"
                        values["app_stdte"] = ""
                        values["lmttime"] = "0" // 밀리세컨드 1분
                        values["spctime"] = "0" // 밀리세컨드 1분
                        values["option1"] = setting.getMode()
                        values["option2"] = "N"

                        dbm.executeInsert("applist", values)

                        Toast.makeText(context, context.getString(R.string.option_new_install_desc), Toast.LENGTH_SHORT).show()
                        common.setLogMsg("New App installed : $packageName, ${setting.isOption9()}, ${setting.getMode()}")
                    }
                } catch (e: Exception) {
                    common.setLogMsg("${intent.action} : $e")
                }
            }
        }

        // 주석 처리된 코드
        /*
        else if (intent.action == PACKAGE_REMOVED || intent.action == PACKAGE_REPLACED) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            val dbm = DbManager(context)
            val common = SkyTimeLockCommon(context)

            try {
                val whereClause = "pkgnme=?"
                val whereArgs = arrayOf(packageName)

                dbm.executeDelete("applist", whereClause, whereArgs)

                common.setLogMsg("${intent.action} : $packageName")
            } catch (e: Exception) {
                common.setLogMsg("SkyInstallReceiver Error : $e")
            }
        }
        */
    }
}