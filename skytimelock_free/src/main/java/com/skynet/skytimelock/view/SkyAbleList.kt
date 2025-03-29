package com.skynet.skytimelock.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.skynet.common.ProgressTask
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.adapter.SkyAppListAdapter
import com.skynet.skytimelock.bean.SkyAppListInfo
import com.skynet.skytimelock.free.R
import java.util.ArrayList
import java.util.HashMap

class SkyAbleList : SkyNetBaseActivity() {
    private lateinit var adapterList_all: ArrayList<SkyAppListInfo>
    private lateinit var adapterList_all_filter: ArrayList<SkyAppListInfo>

    private lateinit var listAdapter_all: SkyAppListAdapter
    private lateinit var mListView_All: ListView

    private lateinit var adapterList_allowed: ArrayList<SkyAppListInfo>
    private lateinit var adapterList_required: ArrayList<SkyAppListInfo>
    private lateinit var db_allowed_list: ArrayList<HashMap<Any, Any>>

    private lateinit var dbm: DbManager
    private lateinit var setting: SkyTimeLockSetting

    private lateinit var btn_check_lock_all: Button
    private lateinit var btn_lock_save: TextView
    private lateinit var btn_lock_delete: TextView
    private lateinit var btn_lock_close: Button

    private lateinit var edt_search_lock_list: EditText

    // 전체 선택
    private var isAllChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(false)
        super.setLayout_Id(R.layout.list_able)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dbm = DbManager(this.applicationContext)
        setting = common.getSetting("SkyAbleList")

        btn_check_lock_all = findViewById<Button>(R.id.btn_check_lock_all)
        btn_check_lock_all.setOnClickListener(btn_clickEvent)

        btn_lock_save = findViewById<TextView>(R.id.txt_save)
        btn_lock_save.setOnClickListener(btn_clickEvent)

        btn_lock_delete = findViewById<TextView>(R.id.txt_delete)
        btn_lock_delete.setOnClickListener(btn_clickEvent)

        btn_lock_close = findViewById<Button>(R.id.btn_lock_close)
        btn_lock_close.setOnClickListener(btn_clickEvent)

        edt_search_lock_list = findViewById<EditText>(R.id.edt_search_lock_list)
        edt_search_lock_list.inputType = 0
        edt_search_lock_list.addTextChangedListener(textWatcherInput)
        edt_search_lock_list.setOnClickListener(clickListener)
        edt_search_lock_list.setOnEditorActionListener(onEditorActionListener)

        getAllAppList()
    }

    /**
     * 클릭 리스너
     */
    private val clickListener = OnClickListener { _ ->
        edt_search_lock_list.inputType = 1
        val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(edt_search_lock_list, InputMethodManager.SHOW_IMPLICIT)
    }

    private val onEditorActionListener = OnEditorActionListener { _, _, _ ->
        searchApp()
        false
    }

    /**
     * 텍스트 변경 감지
     */
    private val textWatcherInput = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (count == 0)
                searchApp()
            else
                searchApp(null, s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    }

    /**
     * 앱 찾기
     */
    private fun searchApp() {
        searchApp(null, null)
    }

    /**
     * 앱 찾기
     *
     * @param gbn
     * @param word
     */
    @SuppressLint("DefaultLocale")
    private fun searchApp(gbn: String?, word: String?) {
        adapterList_all_filter = ArrayList()

        if ((gbn == null && word == null) || "ALL" == gbn) {
            listAdapter_all = SkyAppListAdapter(this, adapterList_all, 9)
            mListView_All.adapter = listAdapter_all
            return
        }

        val ADAPTERLIST = adapterList_all
        val ADAPTERLIST_FILTER = ArrayList<SkyAppListInfo>()

        for (i in 0 until ADAPTERLIST.size) {
            val row = if (word != null && "" != word) {
                ADAPTERLIST[i].getAPP_NAME().toLowerCase().indexOf(word.toLowerCase())
            } else {
                if (ADAPTERLIST[i].getAPP_GBN() == gbn) 1 else -1
            }

            if (row > -1) {
                ADAPTERLIST_FILTER.add(ADAPTERLIST[i])
            }
        }

        listAdapter_all = SkyAppListAdapter(this, ADAPTERLIST_FILTER, 9)
        mListView_All.adapter = listAdapter_all
    }

    private val btn_clickEvent = OnClickListener { v ->
        when (v.id) {
            R.id.btn_check_lock_all -> setChkAll()
            R.id.txt_save -> {
                setSaveAppList(false)
                getAllAppList()
                common.setRefreshSetting(true)
            }
            R.id.txt_delete -> {
                setSaveAppList(true)
                getAllAppList()
                common.setRefreshSetting(true)
            }
            R.id.btn_lock_close -> finish()
        }
    }

    /**
     * 전체 선택
     */
    private fun setChkAll() {
        isAllChecked = !isAllChecked

        listAdapter_all.setAllChecked(isAllChecked)
        listAdapter_all.notifyDataSetChanged()
    }

    private fun getAllAppList() {
        showWaitProg()
        Thread(Runnable {
            try {
                loadAllApps()
            } catch (e: Exception) {
                common.setLogMsg("모든 앱리스트 조회 :: $e")
            }
            mHandler.sendMessage(Message.obtain(mHandler, 1))
        }).start()
    }

    /**
     * 모든 설치된 앱정보를 로드
     */
    private fun loadAllApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val mApps = packageManager.queryIntentActivities(mainIntent, 0)
        val manager = packageManager

        adapterList_all = ArrayList()
        adapterList_allowed = ArrayList()
        adapterList_required = ArrayList()

        loadFromDBAllowedApp()

        for (i in 0 until mApps.size) {
            if (mApps[i].loadLabel(manager) != null || "" != mApps[i].loadLabel(manager).toString().trim()) {
                val pkgname = mApps[i].activityInfo.packageName.trim()

                if (pkgname.startsWith("com.skynet.skytimelock")) continue

                val sInfo = SkyAppListInfo()

                try {
                    sInfo.setAPP_ICON(mApps[i].activityInfo.loadIcon(manager))
                } catch (error: OutOfMemoryError) {
                    sInfo.setAPP_ICON(null)
                }

                sInfo.setAPP_GBN("0")
                sInfo.setAPP_NAME(mApps[i].loadLabel(manager).toString())
                sInfo.setAPP_PKG_NAME(pkgname)
                sInfo.setAPP_DESC(mApps[i].activityInfo.packageName)
                sInfo.setAPP_CHK(false)
                sInfo.setAPP_LMTTIME((10 * 60 * 1000).toString())
                sInfo.setAPP_SPCTIME("0")
                sInfo.setAPP_OPTION1(setting.getMode())
                sInfo.setAPP_OPTION2("N")
                sInfo.setAPP_OPTION3("")
                sInfo.setAPP_OPTION4("")

                var isAllow = false

                // 항상 허용 앱
                for (j in 0 until db_allowed_list.size) {
                    if (db_allowed_list[j]["pkgnme"].toString().trim().equals(pkgname, ignoreCase = true)) {
                        sInfo.setAPP_GBN("9")
                        sInfo.setAPP_ALLOWED(true)

                        adapterList_allowed.add(sInfo)
                        isAllow = true
                        break
                    }
                }

                if (isAllow) continue

                adapterList_all.add(sInfo)
            }
        }

        // Home Launcher 앱
        val homeIntent = Intent(Intent.ACTION_MAIN, null)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        val homeLauncherApps = packageManager.queryIntentActivities(homeIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)

        for (i in 0 until homeLauncherApps.size) {
            if (homeLauncherApps[i].loadLabel(manager) != null || "" != homeLauncherApps[i].loadLabel(manager).toString().trim()) {
                val pkgname = homeLauncherApps[i].activityInfo.packageName.trim()

                val sInfo = SkyAppListInfo()

                try {
                    sInfo.setAPP_ICON(homeLauncherApps[i].activityInfo.loadIcon(manager))
                } catch (error: OutOfMemoryError) {
                    sInfo.setAPP_ICON(null)
                }

                sInfo.setAPP_GBN("0")
                sInfo.setAPP_NAME(getString(R.string.launcher_app) + homeLauncherApps[i].loadLabel(manager).toString())
                sInfo.setAPP_PKG_NAME(pkgname)
                sInfo.setAPP_DESC(homeLauncherApps[i].activityInfo.packageName)
                sInfo.setAPP_CHK(false)
                sInfo.setAPP_LMTTIME((10 * 60 * 1000).toString())
                sInfo.setAPP_SPCTIME("0")
                sInfo.setAPP_OPTION1(setting.getMode())
                sInfo.setAPP_OPTION2("N")
                sInfo.setAPP_OPTION3("")
                sInfo.setAPP_OPTION4("")

                var isAllow = false

                // 항상 허용 앱
                for (j in 0 until db_allowed_list.size) {
                    if (db_allowed_list[j]["pkgnme"].toString().trim().equals(pkgname, ignoreCase = true)) {
                        sInfo.setAPP_GBN("9")
                        sInfo.setAPP_ALLOWED(true)

                        adapterList_allowed.add(sInfo)
                        isAllow = true
                        break
                    }
                }

                if (isAllow) continue

                adapterList_required.add(sInfo)
            }
        }

        for (i in adapterList_required.indices) {
            adapterList_all.add(0, adapterList_required[i])
        }

        for (i in adapterList_allowed.indices) {
            adapterList_all.add(0, adapterList_allowed[i])
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
    private fun loadFromDBAllowedApp() {
        db_allowed_list = ArrayList()
        try {
            db_allowed_list = dbm.executeSelect("SELECT * FROM allowedlist", null) ?: ArrayList()
        } catch (e: Exception) {
            SetLog("loadFromDBAllowedApp :: $e")
        }
    }

    /**
     * ListView 바인딩
     */
    private fun setAllListBinding() {
        mListView_All = findViewById<ListView>(R.id.grd_app_list_lock_list)
        listAdapter_all = SkyAppListAdapter(this, adapterList_all, 9)
        mListView_All.adapter = listAdapter_all
    }

    /**
     * 입력 및 업데이트
     */
    private fun setSaveAppList(isDelete: Boolean) {
        ProgressTask(this@SkyAbleList).execute(listAdapter_all.getCheckedAll())

        for (row in 0 until listAdapter_all.count) {
            if (listAdapter_all.getChecked(row)) {
                // 삭제
                if (isDelete) {
                    setDeleteAppList(row)
                }
                // 저장
                else {
                    // 업데이트 로직
                    val pkg_name = listAdapter_all.getPkgName(row)

                    common.setLogMsg("pkg_name : $pkg_name")

                    var cnt = 0
                    for (i in 0 until db_allowed_list.size) {
                        if (db_allowed_list[i]["pkgnme"].toString().trim().equals(pkg_name, ignoreCase = true)) {
                            cnt++
                            break
                        }
                    }

                    // 업데이트
                    if (cnt > 0) {
                        setUpdateAppList(row)
                    }
                    // 신규 입력
                    else {
                        val hm = HashMap<Any, Any>()
                        hm["pkgnme"] = pkg_name

                        setInsertAppList(row)
                        db_allowed_list.add(hm)
                    }
                }

                listAdapter_all.setUnChecked(row)
            }
        }

        listAdapter_all.notifyDataSetChanged()
        common.StartService()
    }

    /**
     * 삭제
     */
    private fun setDeleteAppList(row: Int) {
        val whereClause = "pkgnme=?"

        val pkg_name = (mListView_All.adapter.getItem(row) as SkyAppListInfo).getAPP_PKG_NAME().trim()
        val whereArgs = arrayOf(pkg_name)

        try {
            for (i in 0 until db_allowed_list.size) {
                if (db_allowed_list[i]["pkgnme"].toString().trim().equals(pkg_name, ignoreCase = true)) {
                    db_allowed_list.removeAt(i)
                    break
                }
            }

            dbm.executeDelete("allowedlist", whereClause, whereArgs)
        } catch (e: Exception) {
            common.setLogMsg("삭제 오류 :: $e")
            return
        }
    }

    /**
     * 선택된 앱 리스트 저장
     */
    @SuppressLint("SimpleDateFormat")
    private fun setInsertAppList(row: Int) {
        val values = HashMap<Any, Any>()

        try {
            val packagename = (mListView_All.adapter.getItem(row) as SkyAppListInfo).getAPP_PKG_NAME()

            values["pkgnme"] = packagename
            dbm.executeInsert("allowedlist", values)
        } catch (e: Exception) {
            common.setLogMsg("저장 오류 :: $e")
            return
        }
    }

    /**
     * 수정
     */
    @SuppressLint("SimpleDateFormat")
    private fun setUpdateAppList(row: Int) {
        val values = HashMap<Any, Any>()

        try {
            val packagename = (mListView_All.adapter.getItem(row) as SkyAppListInfo).getAPP_PKG_NAME()
            val whereClause = "pkgnme=?"
            val whereArgs = arrayOf(packagename)

            values["pkgnme"] = packagename
            dbm.executeUpdate("allowedlist", values, whereClause, whereArgs)
        } catch (e: Exception) {
            common.setLogMsg("수정 오류 :: $e")
            return
        }
    }
}