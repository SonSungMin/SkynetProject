package com.skynet.common

import android.Manifest
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission

import com.skynet.db.DbManager
import com.skynet.skytimelock.free.R
import com.skynet.skytimelock.free.SkyPersistentService

class SkyTimeLockCommon(private val ctx: Context) {
    private lateinit var process_setting: ArrayList<HashMap<Any, Any>>
    private val dbm: DbManager = DbManager(ctx)

    private val setting: SkyTimeLockSetting = SkyTimeLockSetting()

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyy-MM-dd")

    val PHOTO_FILE_PATH = "${Environment.getExternalStorageDirectory()}/skynet/images"

    private val FILE_PATH: String = "/data/data/${ctx.packageName}"

    fun copyDbFile() {
        dbm.copyDbFile()
    }

    /**
     * 국내 유저 구분
     */
    fun isKor(): Boolean {
        return ctx.resources.configuration.locale.country.equals("KR", ignoreCase = true)
    }

    /**
     * 실행중인 서비스 종료
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun killService() {
        killService(100)
    }

    /**
     * 실행중인 서비스 종료
     * @param ea
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Synchronized
    fun killService(ea: Int) {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.getRunningServices(ea)
        var killCnt = 0

        val task = am.getRunningTasks(1)
        val runningPackageName = task[0].topActivity?.packageName.toString()

        for (runningTaskInfo in info) {
            // 프로세스 끝내기
            if (!runningTaskInfo.service.packageName.startsWith("com.skynet.skytimelock") &&
                runningTaskInfo.service.packageName != runningPackageName) {
                try {
                    Process.killProcess(runningTaskInfo.pid)
                    am.killBackgroundProcesses(runningTaskInfo.service.packageName)
                    am.restartPackage(runningTaskInfo.service.packageName)
                    killCnt++
                } catch (e: Exception) {
                    setLogMsg(e.toString())
                }
            }
        }
        Toast.makeText(ctx, ctx.getString(R.string.kill_process) + killCnt + ctx.getString(R.string.unit_ea), Toast.LENGTH_SHORT).show()
    }

    /**
     * 서비스 실행
     */
    fun startService() {
        setIsInit(true)
        setRefreshSetting(true)

        //this.ctx.stopService(new Intent(this.ctx, SkyPersistentService.class));
        // 서비스 시작
        ctx.startService(Intent(ctx, SkyPersistentService::class.java))
    }

    /**
     * 로그 기록
     * @param msg
     */
    fun setLogMsg(msg: String) {
        Log.e("SkyNet", "### $msg")
    }

    /**
     * 환경 설정 로드
     */
    fun getSetting(msg: String): SkyTimeLockSetting {
        var mode: Any? = null
        var lock_mode: Any? = null
        var bg: Any? = null
        var pwd: Any? = null
        var pattern: Any? = null
        var prior_value: Any? = null
        var time_start: Any? = null
        var time_end: Any? = null
        var bluelight: Any? = null
        var statusicon: Any? = null
        var smslock: Any? = null
        var smsunlockmsg: Any? = null
        var option1: Any? = null
        var option2: Any? = null
        var option3: Any? = null
        var option4: Any? = null
        var option5: Any? = null
        var option6: Any? = null
        var option7: Any? = null
        var option8: Any? = null
        var option9: Any? = null
        var option10: Any? = null

        val settings = dbm.executeSelect("select * from appsetting where uid = 'skynet'", null)

        if (settings != null && settings.size > 0) {
            mode = settings[0]["mode"]
            lock_mode = settings[0]["lock_mode"]
            bg = settings[0]["bg"]
            pwd = settings[0]["pwd"]
            pattern = settings[0]["pattern"]
            prior_value = settings[0]["prior_value"]

            statusicon = settings[0]["statusicon"].toString()
            smslock = settings[0]["smslock"].toString()
            smsunlockmsg = settings[0]["smsunlockmsg"].toString()

            time_start = settings[0]["time_start"].toString()
            time_end = settings[0]["time_end"].toString()

            try {
                bluelight = settings[0]["bluelight"].toString()
            } catch (e: Exception) {
                bluelight = "30"
            }

            option1 = settings[0]["option1"]
            option2 = settings[0]["option2"]
            option3 = settings[0]["option3"]
            option4 = settings[0]["option4"]
            option5 = settings[0]["option5"]
            option6 = settings[0]["option6"]
            option7 = settings[0]["option7"]
            option8 = settings[0]["option8"]
            option9 = settings[0]["option9"]
            option10 = settings[0]["option10"]
        }

        setting.setMode(getString(mode, "2"))
        setting.setLock_mode(getString(lock_mode, "1"))
        setting.setBg(getString(bg, "1"))
        setting.setPwd(getString(pwd, "777"))
        setting.setPattern(getString(pattern, ""))
        setting.setPrior_value(getString(prior_value, "1|1|1|1|1|1|1|"))

        setting.setStatusicon(getString(statusicon, "1"))
        setting.setSmslock(getString(smslock, "lock777"))
        setting.setSmsunlockmsg(getString(smsunlockmsg, "password777"))

        setting.setTime_start(getString(time_start, "00:00"))
        setting.setTime_end(getString(time_end, "23:59"))

        setting.setBluelight(getString(bluelight, "30"))

        setting.setOption1(getBoolean(option1, true))
        setting.setOption2(getBoolean(option2, true))
        setting.setOption3(getBoolean(option3, false))
        setting.setOption4(getBoolean(option4, true))
        setting.setOption5(getBoolean(option5, true))
        setting.setOption6(getBoolean(option6, false))
        setting.setOption7(getBoolean(option7, true))
        setting.setOption8(getBoolean(option8, false))
        setting.setOption9(getBoolean(option9, false))
        setting.setOption10(getBoolean(option10, true))

        setLogMsg("### read setting from [$msg] ###")

        return setting
    }

    /**
     * 환경 설정 저장
     *
     * @param setValue SkyTimeLockSetting
     */
    fun setSetting(setValue: SkyTimeLockSetting) {
        try {
            val values = arrayOfNulls<Any>(23)
            var query = ""
            var index = 0

            values[index++] = "skynet"
            values[index++] = setValue.getMode()
            values[index++] = setValue.getLock_mode()
            values[index++] = setValue.getBg()
            values[index++] = setValue.getPwd()
            values[index++] = setValue.getPattern()
            values[index++] = setValue.getPrior_value()
            values[index++] = setValue.getStatusicon()
            values[index++] = setValue.getSmslock()
            values[index++] = setValue.getSmsunlockmsg()
            values[index++] = setValue.isOption1()
            values[index++] = setValue.isOption2()
            values[index++] = setValue.isOption3()
            values[index++] = setValue.isOption4()
            values[index++] = setValue.isOption5()
            values[index++] = setValue.isOption6()
            values[index++] = setValue.isOption7()
            values[index++] = setValue.isOption8()
            values[index++] = setValue.isOption9()
            values[index++] = setValue.isOption10()
            values[index++] = setValue.getTime_start()
            values[index++] = setValue.getTime_end()
            values[index++] = setValue.getBluelight()

            query = "INSERT OR REPLACE INTO appsetting(uid,mode,lock_mode,bg,pwd,pattern,prior_value,statusicon,smslock,smsunlockmsg,option1,option2,option3,option4,option5,option6,option7,option8,option9,option10,time_start,time_end,bluelight) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

            val rst = dbm.executeSave(query, values)

            if (!rst) {
                setLogMsg("환경설정 저장 오류")
            }
        } catch (e: Exception) {
            setLogMsg("환경설정 저장 오류 : ${e.message}")
        }
    }

    /**
     * 상태바 아이콘 설정
     * @return
     */
    fun getStatusIcon(): String? {
        val setting = getSetting("common.getStatusIcon")
        return setting.getStatusicon() // 소문자 'i'로 호출
    }

    /**
     * 상태바 아이콘 설정
     * @param icon
     */
    fun setStatusIcon(icon: String) {
        val table = "appsetting"
        val values = HashMap<Any, Any>()
        val whereClause = null
        val whereArgs = null

        values["statusicon"] = icon
        dbm.executeUpdate(table, values, whereClause, whereArgs)
    }

    /**
     * SMS 수신시 잠금 문자 반환
     * @return
     */
    fun getSMSLock(): String? {
        val setting = getSetting("common.getSMSLock")
        return setting.getSmslock()
    }

    /**
     * SMS 수신시 잠금 문자 설정
     * @param lockMsg
     */
    fun setSMSLock(lockMsg: String) {
        val table = "appsetting"
        val values = HashMap<Any, Any>()
        val whereClause = null
        val whereArgs = null

        values["smslock"] = lockMsg
        dbm.executeUpdate(table, values, whereClause, whereArgs)
    }

    /**
     * SMS UnLock 메시지
     * @return
     */
    fun getSMSUnLockMsg(): String? {
        val setting = getSetting("common.getSMSUnLockMsg")
        return setting.getSmsunlockmsg()
    }

    /**
     * SMS UnLock 메시지
     */
    fun setSMSUnLockMsg(unlockMsg: String) {
        val table = "appsetting"
        val values = HashMap<Any, Any>()
        val whereClause = null
        val whereArgs = null

        values["smsunlockmsg"] = unlockMsg
        dbm.executeUpdate(table, values, whereClause, whereArgs)
    }

    // ######################################################################
    private fun getString(obj: Any?, value: String): String {
        return when {
            obj == null || obj.toString() == null || obj == "" || obj == "null" -> value
            else -> obj.toString()
        }
    }

    private fun getLong(obj: Any?, value: Long): Long {
        return when {
            obj == null || obj.toString() == null || obj == "" || obj == "null" -> value
            else -> obj.toString().toLong()
        }
    }

    private fun getBoolean(obj: Any?, value: Boolean): Boolean {
        return try {
            when {
                obj == null || obj.toString() == null || obj == "" || obj == "null" -> value
                obj is Boolean -> obj
                obj is String -> obj == "1" || obj == "true"
                else -> value
            }
        } catch (e: Exception) {
            value
        }
    }
    // ######################################################################

    // ########################################################################### 운영에 필요한 정보 저장 :: Start

    fun getProcessSetting(field_name: String): Any? {
        var s = ""

        try {
            val file = File("$FILE_PATH/$field_name")

            if (!file.exists())
                file.createNewFile()

            val reader = BufferedReader(FileReader("$FILE_PATH/$field_name"))
            s = reader.readLine() ?: ""
            reader.close()
        } catch (e: IOException) {
            setLogMsg("getProcessSetting : ${e.message}")
        }

        return s
    }

    fun setProcessSetting(field_name: String, value: Any) {
        try {
            val path = "$FILE_PATH/$field_name"
            val file = File(path)
            if (!file.exists())
                file.createNewFile()

            val writer = BufferedWriter(FileWriter(path))
            writer.write(value.toString())
            writer.close()
        } catch (e: Exception) {
            setLogMsg("setProcessSetting : ${e.message}")
        }
    }

    /**
     * 상태바 아이콘 변경
     * @return
     */
    fun getChangeStatusIcon(): Boolean {
        return getBoolean(getProcessSetting("changestatusicon"), true)
    }

    /**
     * 상태바 아이콘 변경
     * @param value
     */
    fun setChangeStatusIcon(value: Boolean) {
        setProcessSetting("changestatusicon", value)
    }

    /**
     * 변경된 환경설정파일을 갱신
     * @return
     */
    fun getRefreshSetting(): Boolean {
        return getBoolean(getProcessSetting("refreshsetting"), true)
    }

    /**
     * 변경된 환경설정파일 갱신 여부 설정
     * @param value
     */
    fun setRefreshSetting(value: Boolean) {
        setProcessSetting("refreshsetting", value)
    }

    /**
     * [일일 앱잠금]시간 제한 설정 시간
     * @return
     */
    fun getLockTime(): Long {
        return getLong(getProcessSetting("locktime"), 60 * 60 * 1000)
    }

    /**
     * [일일 앱잠금]시간 제한 설정 시간
     * @param time
     */
    fun setLockTime(time: Long) {
        setProcessSetting("locktime", time)
    }

    /**
     * [일일 앱잠금]날짜
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getLockDate(): String {
        val dTime = formatter.format(Date(System.currentTimeMillis()))
        var rtn = getString(getProcessSetting("lockdate"), "")

        if (rtn == "") {
            rtn = dTime
            setLockDate()
        }
        return rtn
    }

    /**
     * [일일 앱잠금]날짜
     */
    fun setLockDate() {
        val dTime = formatter.format(Date())
        setProcessSetting("lockdate", dTime)
    }

    /**
     * SMS수신시 모두 잠금 여부
     * @return
     */
    fun getAllLock(): Boolean {
        return getBoolean(getProcessSetting("alllock"), false)
    }

    /**
     * SMS수신시 모두 잠금 여부
     * @param lock
     */
    fun setAllLock(lock: Boolean) {
        setProcessSetting("alllock", lock)
    }

    /**
     * 서비스가 최초 실행인지 여부
     * @return
     */
    fun getIsInit(): Boolean {
        return getBoolean(getProcessSetting("isinit"), true)
    }

    /**
     * 서비스가 최초 실행인지 여부
     * @param isInit
     */
    fun setIsInit(isInit: Boolean) {
        setProcessSetting("isinit", isInit)
        setLogMsg("IsInit = $isInit")
    }

    /**
     * 와이파이 접속된 경우만 실행 여부
     * @return
     */
    fun getIsCheckedWiFi(): Boolean {
        return getBoolean(getProcessSetting("ischeckedwifi"), false)
    }

    /**
     * 와이파이 접속된 경우만 실행 여부
     * @param isCheckedWiFi
     */
    fun setIsCheckedWiFi(isCheckedWiFi: Boolean) {
        setProcessSetting("ischeckedwifi", isCheckedWiFi)
    }

    /**
     * 암호를 제대로 입력했는지 여부
     * @return
     */
    fun getIsCheckedPwd(): Boolean {
        return getBoolean(getProcessSetting("ischeckedpwd"), false)
    }

    /**
     * 암호를 제대로 입력했는지 여부
     * @param isCheckedPwd
     */
    fun setIsCheckedPwd(isCheckedPwd: Boolean) {
        setProcessSetting("ischeckedpwd", isCheckedPwd)
    }

    fun getScreenOn(): Boolean {
        return getBoolean(getProcessSetting("screenon"), true)
    }

    fun setScreenOn(screenOn: Boolean) {
        setProcessSetting("screenon", screenOn)
    }

    /**
     * 암호 초기화
     */
    fun setInitPwd() {
        val table = "appsetting"
        val values = HashMap<Any, Any>()
        val whereClause = null
        val whereArgs = null

        values["pwd"] = "777"
        values["pattern"] = ""
        dbm.executeUpdate(table, values, whereClause, whereArgs)
    }

    // ########################################################################### 운영에 필요한 정보 저장 :: End

    /**
     * 저장된 배경 이미지 경로 반환
     *
     * @return
     */
    fun getPhotoList(): Array<String>? {
        var list: Array<String>? = null

        try {
            val file = File(PHOTO_FILE_PATH)
            if (!file.exists())
                return null

            val files = file.listFiles()

            list = Array(files.size) { i ->
                files[i].absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    /**
     * 환경설정에 따른 배경 이미지 리턴
     *
     * @return
     */
    @Synchronized
    fun getBackgroundImg(): Drawable? {
        var image: Drawable? = null

        try {
            val BG_LIST = getPhotoList()
            image = getDefaultBg()

            var index = -1

            // 등록된 배경 이미지가 있을 경우
            if (BG_LIST != null && BG_LIST.isNotEmpty()) {
                // 무작위 배경인 경우
                if (setting.isOption3()) {
                    index = (Math.random() * (BG_LIST.size - 1)).toInt()
                    try {
                        image = BitmapDrawable(BitmapFactory.decodeFile(BG_LIST[index]))
                    } catch (e: Exception) {
                        image = null
                        setLogMsg("getBackgroundImg = ${e}")
                    }
                } else {
                    if (setting.getBg() != "1") {
                        image = BitmapDrawable(BitmapFactory.decodeFile(setting.getBg()))
                    }
                }
            }
        } catch (e: Exception) {
            setLogMsg("getBackgroundImg = ${e}")
        }

        return image
    }

    private fun getDefaultBg(): Drawable {
        return ctx.wallpaper
    }

    fun getThumbnailImg(imagePath: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = 8
            inPurgeable = true
            inDither = true
        }

        val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: run {
            setLogMsg("$imagePath is null")
            Toast.makeText(ctx, "Error!!\nPlease try again", Toast.LENGTH_SHORT).show()
            return null
        }

        var scale = 0f

        if (bitmap.width > bitmap.height) {
            scale = 100.0f / bitmap.width

            if (bitmap.height * scale < 100)
                scale = 100.0f / bitmap.height
        } else {
            scale = 100.0f / bitmap.height

            if (bitmap.width * scale < 100)
                scale = 100.0f / bitmap.width
        }

        val pX = (bitmap.width * scale).toInt()
        val pY = (bitmap.height * scale).toInt()

        val resize = Bitmap.createScaledBitmap(bitmap, pX, pY, true)
        val sliceBmp = Bitmap.createBitmap(resize, (pX / 2) - 50, (pY / 2) - 50, 100, 100)

        bitmap.recycle()
        resize.recycle()

        return sliceBmp
    }
}