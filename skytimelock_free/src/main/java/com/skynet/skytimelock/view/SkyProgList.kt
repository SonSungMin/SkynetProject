package com.skynet.skytimelock.view

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.adapter.SkyProgListAdapter
import com.skynet.skytimelock.bean.SkyProgListInfo
import com.skynet.skytimelock.free.R

class SkyProgList : SkyNetBaseActivity() {
    private lateinit var adapterList_all: ArrayList<SkyProgListInfo>

    private lateinit var listAdapter_all: SkyProgListAdapter
    private lateinit var mListView_All: ListView

    private lateinit var adapterList_time: ArrayList<SkyProgListInfo>
    private lateinit var db_select_list: ArrayList<HashMap<Object, Object>>

    private lateinit var dbm: DbManager
    private lateinit var setting: SkyTimeLockSetting

    private lateinit var btn_pre: Button
    private lateinit var btn_next: Button

    private lateinit var txt_date: TextView

    private val fmt = SimpleDateFormat("yyyy-MM-dd")
    private val cal = GregorianCalendar()

    private var exetime: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(false)
        super.setLayout_Id(R.layout.list_prog)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dbm = DbManager(this.applicationContext)
        setting = common.getSetting("SkyAbleList")

        btn_pre = findViewById(R.id.btn_pre)
        btn_pre.setOnClickListener(btn_clickEvent)

        btn_next = findViewById(R.id.btn_next)
        btn_next.setOnClickListener(btn_clickEvent)

        txt_date = findViewById(R.id.txt_date)

        val setDate = fmt.format(Date())
        txt_date.text = setDate

        getAppList(null)
    }

    private val btn_clickEvent = OnClickListener { v ->
        val date: Date
        val setDate: String

        when (v.id) {
            R.id.btn_pre -> {
                cal.add(Calendar.DATE, -1)
                date = cal.time
                setDate = fmt.format(date)
                txt_date.text = setDate

                getAppList(setDate)
            }

            R.id.btn_next -> {
                cal.add(Calendar.DATE, 1)
                date = cal.time
                setDate = fmt.format(date)
                txt_date.text = setDate

                getAppList(setDate)
            }
        }
    }

    private fun getAppList(date: String?) {
        showWaitProg()

        Thread {
            try {
                loadAllApps(date)
            } catch (e: Exception) {
                common.setLogMsg("모든 앱리스트 조회 :: $e")
            }
            mHandler.sendMessage(Message.obtain(mHandler, 1))
        }.start()
    }

    /**
     * 모든 설치된 앱정보를 로드
     */
    private fun loadAllApps(date: String?) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        var exetime_app: Float = 0f
        var rate: Long = 0
        var run = 0

        val mApps: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)

        val manager = packageManager

        adapterList_all = ArrayList()
        adapterList_time = ArrayList()

        loadFromDBAllowedApp(date)
        var isAllow: Boolean

        for (i in mApps.indices) {
            if (mApps[i].loadLabel(manager) != null || "" != mApps[i].loadLabel(manager).toString().trim()) {
                val pkgname = mApps[i].activityInfo.packageName.trim()

                val sInfo = SkyProgListInfo()

                try {
                    sInfo.setAPP_ICON(mApps[i].activityInfo.loadIcon(manager))
                } catch (error: OutOfMemoryError) {
                    sInfo.setAPP_ICON(null)
                }
                sInfo.setAPP_NAME(mApps[i].loadLabel(manager).toString())
                sInfo.setAPP_PKG_NAME(pkgname)
                sInfo.setAPP_GRAPH(0)
                sInfo.setAPP_RATE(0)
                sInfo.setAPP_USE_TIME("00:00:00")
                sInfo.setAPP_RUN(0)

                isAllow = false

                // 사용 시간이 있는 앱
                for (j in 0 until db_select_list.size) {
                    if (db_select_list[j]["pkgnme"].toString().trim().equals(pkgname, ignoreCase = true)) {
                        try {
                            run = Integer.parseInt(db_select_list[j]["execnt"].toString())
                        } catch (e: Exception) {
                            run = 0
                            common.setLogMsg("execnt = " + db_select_list[j]["cnt"])
                        }

                        exetime_app = java.lang.Long.parseLong(db_select_list[j]["exetime"].toString()).toFloat()
                        rate = Math.round(exetime_app / exetime * 100)

                        sInfo.setAPP_GRAPH(rate.toInt())
                        sInfo.setAPP_RATE(rate.toInt())
                        sInfo.setAPP_USE_TIME(getCountDown(exetime_app.toDouble()))
                        sInfo.setAPP_INDEX(j)
                        sInfo.setAPP_RUN(run)

                        adapterList_time.add(sInfo)
                        isAllow = true
                        break
                    }
                }

                if (isAllow) continue

                adapterList_all.add(sInfo)
            }
        }

        if (::adapterList_time.isInitialized) {
            Collections.sort(adapterList_time, DescCompare())

            for (i in 0 until adapterList_time.size) {
                adapterList_all.add(0, adapterList_time[i])
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                // 전체 어플 리스트
                1 -> setAllListBinding()
            }
            hideWaitProg()
        }
    }

    /**
     * DB에 저장된 내용 조회(항상 허용 앱)
     */
    @SuppressLint("SimpleDateFormat")
    private fun loadFromDBAllowedApp(date: String?) {
        try {
            db_select_list = ArrayList()

            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val dTime: String = date ?: formatter.format(Date())

            var sql = "select sum(exetime) as exetime from appcount where exedte = ? and pkgnme <> '1'"
            var selectionArgs = arrayOf(dTime)

            val select = dbm.executeSelect(sql, selectionArgs)

            if (select != null && select.isNotEmpty() && select[0]["exetime"] != null) {
                exetime = java.lang.Long.parseLong(select[0]["exetime"].toString()).toFloat()
            }

            sql = "select pkgnme, exetime, execnt from appcount where exedte = ? and pkgnme <> '1' order by exetime desc"
            selectionArgs = arrayOf(dTime)

            db_select_list = dbm.executeSelect(sql, selectionArgs)
        } catch (e: Exception) {
            SetLog("loadFromDBAllowedApp :: $e")
        }
    }

    /**
     * ListView 바인딩
     */
    private fun setAllListBinding() {
        mListView_All = findViewById(R.id.grd_prog_list)
        listAdapter_all = SkyProgListAdapter(this, adapterList_all)
        mListView_All.adapter = listAdapter_all
    }

    private fun getCountDown(USETIME: Double): String {
        val value = StringBuilder()
        value.append(getHH(USETIME))
        value.append(":")
        value.append(getMM(USETIME))
        value.append(":")
        value.append(getSEC(USETIME))
        return value.toString()
    }

    private fun getHH(USETIME: Double): String {
        val time = ((USETIME / (1000 * 60 * 60)) % 24).toInt()

        return if (time < 10) {
            "0$time"
        } else {
            "$time"
        }
    }

    private fun getMM(USETIME: Double): String {
        val time = ((USETIME / (1000 * 60)) % 60).toInt()

        return if (time < 10) {
            "0$time"
        } else {
            "$time"
        }
    }

    private fun getSEC(USETIME: Double): String {
        val time = ((USETIME / 1000) % 60).toInt()

        return if (time < 10) {
            "0$time"
        } else {
            "$time"
        }
    }

    /**
     * 구분 내림차순
     */
    private class DescCompare : Comparator<SkyProgListInfo> {
        override fun compare(arg0: SkyProgListInfo, arg1: SkyProgListInfo): Int {
            return when {
                arg0.getAPP_RATE() < arg1.getAPP_RATE() -> -1
                arg0.getAPP_RATE() > arg1.getAPP_RATE() -> 1
                else -> 0
            }
        }
    }
}