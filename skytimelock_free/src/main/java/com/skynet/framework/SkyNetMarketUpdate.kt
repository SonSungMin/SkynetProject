package com.skynet.framework

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.util.Log

import com.skynet.common.SkyTimeLockCommon
import com.skynet.skytimelock.free.R

class SkyNetMarketUpdate(private val ctx: Context) : AsyncTask<String, Void, Void>() {
    private var mDialog: AlertDialog? = null

    private var packageName = ""
    private var packageVersion = 0
    private var marketVersion = 0

    private var marketContent = ""
    private var runCnt = 0

    override fun doInBackground(vararg params: String): Void? {
        try {
            val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            packageName = packageInfo.packageName
            packageVersion = packageInfo.versionCode

            try {
                val url = URL(params[0])
                val conn = url.openConnection() as HttpURLConnection

                conn.apply {
                    connectTimeout = 10000
                    useCaches = false

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val br = BufferedReader(InputStreamReader(inputStream, "euc-kr"))

                        while (true) {
                            val line = br.readLine() ?: break

                            if (line.startsWith("version:")) {
                                marketVersion = line.split(":")[1].toInt()
                            } else {
                                marketContent += "$line\n"
                            }
                        }
                        br.close()
                    }
                    disconnect()
                }
            } catch (ex: Exception) {
                Log.e("", "### SkyNet ### 마켓 업데이트 체크 오류 $ex")
            }
        } catch (e: Exception) {
            val common = SkyTimeLockCommon(ctx)
            common.startService()
            Log.e("", "### SKY ### CheckMarketUpdate : $e")
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        Log.e("", "### SkyNet ###  $packageVersion , $marketVersion")
        if (marketContent.isNotEmpty() && packageVersion < marketVersion)
            createDialog().show()

        runCnt++
    }

    private fun createDialog(): AlertDialog {
        val ab = AlertDialog.Builder(ctx).apply {
            setTitle(ctx.getString(R.string.txt_update))
            setMessage(marketContent)
            setCancelable(true)
            setIcon(ctx.resources.getDrawable(R.drawable.main_icon))

            setPositiveButton("Update") { _, _ ->
                val marketLaunch = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=${ctx.packageName}")
                }
                ctx.startActivity(marketLaunch)
                setDismiss(mDialog)
            }

            setNegativeButton("Cancel") { _, _ ->
                setDismiss(mDialog)
            }
        }

        return ab.create()
    }

    private fun setDismiss(dialog: Dialog?) {
        if (dialog != null && dialog.isShowing)
            dialog.dismiss()
    }

    companion object {
        private val URI = Uri.parse("content://com.google.android.gsf.gservices")
        private const val ID_KEY = "android_id"

        fun getAndroidId(context: Context): String? {
            val params = arrayOf(ID_KEY)
            val c = context.contentResolver.query(URI, null, null, params, null) ?: return null

            if (!c.moveToFirst() || c.columnCount < 2)
                return null

            return try {
                // 수정된 부분: 코틀린 방식으로 16진수 변환
                c.getString(1).toLong().toString(16)
            } catch (e: NumberFormatException) {
                null
            } finally {
                c.close()
            }
        }
    }
}