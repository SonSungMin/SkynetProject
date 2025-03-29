package com.skynet.skytimelock.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.skynet.common.ProgressTask
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.skytimelock.adapter.SkyAppListAdapter
import com.skynet.skytimelock.bean.SkyAppListInfo
import com.skynet.skytimelock.free.R
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

@SuppressLint("SimpleDateFormat")
class SkyAppList : Activity() {
    private lateinit var mListView_All: ListView

    private lateinit var adapterList_all: ArrayList<SkyAppListInfo>
    private lateinit var adapterList_all_filter: ArrayList<SkyAppListInfo>
    private lateinit var adapterList_lock: ArrayList<SkyAppListInfo>
    private lateinit var adapterList_time: ArrayList<SkyAppListInfo>
    /**
     * 추천 잠금 앱
     */
    private lateinit var adapterList_rec_lock: ArrayList<SkyAppListInfo>

    private lateinit var listAdapter_all: SkyAppListAdapter

    private lateinit var edt_search_app: EditText

    private var SELECTED_APP = 0

    /**
     * 전체 설정 앱 리스트
     */
    private lateinit var db_select_list: ArrayList<HashMap<Object, Object>>
    /**
     * 허용 가능 앱 리스트
     */
    private lateinit var db_allowed_list: ArrayList<HashMap<Object, Object>>

    private var PKGIMG: Drawable? = null
    private var PKGNAME: String = ""
    private var PKGID: String = ""
    private var LMTTIME: String = ""
    private var SELECTED_METHOD: String = ""
    private var OPTION1: String = ""
    private var OPTION2: String = ""
    private var SPCTIME: String = ""
    private var ISCHECKEDAPP = false

    private var isAllChecked = false
    private var type: Int = 0       // 저장된 타입
    private var lmttime: Long = 0   // 설정한 시간(밀리세컨드)
    private var dayweek: String = ""
    private var option1: String = ""
    private var option2: String = ""

    /**
     * 2:전체, 3:시간제한, 4:잠금
     */
    private var SELECTED_INDEX = 2

    private lateinit var dbm: DbManager
    private lateinit var setting: SkyTimeLockSetting
    private lateinit var common: SkyTimeLockCommon

    private lateinit var btn_check_all: Button
    private lateinit var btn_app_setting: Button

    private lateinit var txt_title: TextView

    /**
     * 어플 구분 1:시간제한, 2:잠금
     */
    private var APP_GBN: String = ""

    private val listview_id = R.id.grd_app_list_all_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lpWindow = WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 1.0f
        }
        window.attributes = lpWindow

        dbm = DbManager(this)
        common = SkyTimeLockCommon(this)
        setting = common.getSetting("SkyAppList")

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.list_app)

        SELECTED_INDEX = intent.getIntExtra("SELECTED_INDEX", 2)

        txt_title = findViewById(R.id.txt_title)

        btn_check_all = findViewById(R.id.btn_check_all)
        btn_check_all.setOnClickListener(btn_clickEvent)

        btn_app_setting = findViewById(R.id.btn_app_setting)
        btn_app_setting.setOnClickListener(btn_clickEvent)

        initApp()
    }

    private fun initApp() {
        edt_search_app = findViewById(R.id.edt_search_all_list)
        edt_search_app.inputType = 0
        edt_search_app.addTextChangedListener(textWatcherInput)
        edt_search_app.setOnClickListener(clickListener)
        edt_search_app.setOnEditorActionListener(onEditorActionListener)

        when(SELECTED_INDEX) {
            // 전체 리스트
            2 -> txt_title.text = getString(R.string.title_all)

            // 시간 제한
            3 -> txt_title.text = getString(R.string.title_timelock)

            // 잠금
            4 -> txt_title.text = getString(R.string.title_lock)
        }

        closeKeyPad()
        getAppList()
    }

    /**
     * 모든 설치된 앱정보를 로드
     */
    private fun loadAllApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        var cntEmpty = 0
        var cntLockEmpty = 0

        val mApps: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
        val manager = packageManager

        adapterList_all = ArrayList()
        adapterList_rec_lock = ArrayList()
        adapterList_time = ArrayList()
        adapterList_lock = ArrayList()

        loadFromDB(null)
        loadFromDBAllowedApp()

        for(i in 0 until mApps.size) {
            if(mApps[i].loadLabel(manager) != null || mApps[i].loadLabel(manager).toString().trim() != "") {
                val pkgname = mApps[i].activityInfo.packageName.trim()

                if(pkgname.startsWith("com.skynet.skytimelock")) continue

                val sInfo = SkyAppListInfo().apply {
                    try {
                        APP_ICON = mApps[i].activityInfo.loadIcon(manager)
                    } catch(error: OutOfMemoryError) {
                        APP_ICON = null
                    }
                    APP_NAME = mApps[i].loadLabel(manager).toString()
                    APP_PKG_NAME = mApps[i].activityInfo.packageName
                    APP_DESC = mApps[i].activityInfo.packageName
                    APP_CHK = false
                    APP_GBN = "0"
                    APP_LMTTIME = (10*60*1000).toString()
                    APP_SPCTIME = "0"
                }

                // 전체 또는 잠금 화면인 경우
                if(SELECTED_INDEX == 2 || SELECTED_INDEX == 4) {
                    ////////////////////////////////////////////////////////////////////
                    // 추천 잠금 앱
                    if(pkgname == "com.android.vending" || pkgname == "com.android.settings") {
                        // 이미 설정된 경우가 아니면
                        if(!getSettingApp(pkgname)) {
                            // 추천 필수 잠금 어플
                            sInfo.APP_REQUIRED = true

                            sInfo.APP_GBN = "99"
                            sInfo.APP_CHK = true
                            sInfo.APP_OPTION1 = setting.getMode()
                            sInfo.APP_OPTION2 = "N"
                            sInfo.APP_OPTION3 = ""
                            sInfo.APP_OPTION4 = ""

                            adapterList_rec_lock.add(sInfo)
                            continue
                        }
                    }
                    ////////////////////////////////////////////////////////////////////

                    val isSetedApp = getSettingApp(pkgname)
                    // 잠금 어플 추출
                    if(isSetedApp && APP_GBN == "2") {
                        sInfo.APP_GBN = APP_GBN
                        sInfo.APP_OPTION1 = OPTION1
                        sInfo.APP_OPTION2 = OPTION2

                        adapterList_lock.add(sInfo)
                        cntLockEmpty++
                        continue
                    }
                }
                // 시간 제한인 경우
                else if(SELECTED_INDEX == 3) {
                    val isSetedApp = getSettingApp(pkgname)

                    // 시간제한 어플 추출
                    if(isSetedApp && APP_GBN == "1") {
                        sInfo.APP_GBN = APP_GBN
                        sInfo.APP_OPTION1 = OPTION1
                        sInfo.APP_OPTION2 = OPTION2
                        sInfo.APP_OPTION3 = ""
                        sInfo.APP_OPTION4 = ""
                        sInfo.APP_LMTTIME = LMTTIME
                        sInfo.APP_SPCTIME = SPCTIME

                        adapterList_time.add(sInfo)
                        cntEmpty++
                        continue
                    }
                }

                // 설정된 어플인 경우 중복 추가하지 않는다.
                if(getSettingApp(pkgname)) continue

                adapterList_all.add(sInfo)
            }
        }

        // 시간제한 앱인경우 처음 조회할때 아무것도 없는 관계로 빈것을 넣어준다.
        if(SELECTED_INDEX == 3) {
            if(cntEmpty == 0) {
                val sInfo = SkyAppListInfo().apply {
                    APP_PKG_NAME = ""
                    APP_GBN = "1"
                    ISEMPTY = true
                }
                adapterList_time.add(sInfo)
            }

            for(i in adapterList_time.indices) {
                adapterList_all.add(0, adapterList_time[i])
            }
        } else {
            if(cntLockEmpty == 0) {
                val sInfo = SkyAppListInfo().apply {
                    APP_GBN = "2"
                    ISEMPTY = true
                }
                adapterList_lock.add(sInfo)
            }

            for(i in adapterList_rec_lock.indices) {
                adapterList_all.add(0, adapterList_rec_lock[i])
            }

            for(i in adapterList_lock.indices) {
                adapterList_all.add(0, adapterList_lock[i])
            }
        }
    }

    /**
     * 설정된 어플인지
     * @param pkgname
     * @return true : 설정된 어플, false : 미설정 어플
     */
    private fun getSettingApp(pkgname: String): Boolean {
        var cnt = 0

        if(::db_select_list.isInitialized && db_select_list.size > 0) {
            cnt = 0

            for(j in 0 until db_select_list.size) {
                if(db_select_list[j]["pkgnme"].toString().trim().equals(pkgname, ignoreCase = true)) {
                    cnt++
                    APP_GBN = db_select_list[j]["exe_gbn"].toString()
                    OPTION1 = db_select_list[j]["option1"].toString()
                    OPTION2 = db_select_list[j]["option2"].toString()
                    LMTTIME = db_select_list[j]["lmttime"].toString()
                    SPCTIME = db_select_list[j]["spctime"].toString()

                    break
                }
            }
        }

        return cnt > 0
    }

    /**
     * DB에 저장된 내용 조회
     * @param param 1 : 시간제한, 2 : 잠금
     */
    private fun loadFromDB(param: String?) {
        db_select_list = ArrayList()
        try {
            db_select_list = if(param == null)
                dbm.executeSelect("SELECT * FROM applist", null)
            else
                dbm.executeSelect("SELECT * FROM applist WHERE exe_gbn = ?", arrayOf(param))
        } catch(e: Exception) {
            common.setLogMsg("loadFromDB :: $e")
        }
    }

    /**
     * DB에 저장된 내용 조회(항상 허용 앱)
     */
    private fun loadFromDBAllowedApp() {
        db_allowed_list = ArrayList()
        try {
            db_allowed_list = dbm.executeSelect("SELECT * FROM allowedlist", null)
        } catch(e: Exception) {
            common.setLogMsg("loadFromDBAllowedApp :: $e")
        }
    }

    /**
     * 앱 리스트 조회
     */
    private fun getAppList() {
        showWaitProg()

        Thread {
            try {
                loadAllApps()
            } catch(e: Exception) {
                common.setLogMsg("모든 앱리스트 조회 :: $e")
            }
            mHandler.sendMessage(Message.obtain(mHandler, SELECTED_INDEX))
        }.start()
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            setListBinding()
            hideWaitProg()
        }
    }

    /**
     * ListView 바인딩
     */
    private fun setListBinding() {
        mListView_All = findViewById(listview_id)
        listAdapter_all = SkyAppListAdapter(this, adapterList_all, SELECTED_INDEX)
        mListView_All.adapter = listAdapter_all
    }

    /**
     * 클릭 리스너
     */
    private val clickListener = View.OnClickListener {
        edt_search_app.inputType = 1
        val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(edt_search_app, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 텍스트 변경 감지
     */
    private val textWatcherInput = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(count == 0)
                searchApp()
            else
                searchApp(null, s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {}
    }

    private val onEditorActionListener = OnEditorActionListener { _, _, _ ->
        searchApp()
        false
    }

    private val btn_clickEvent = View.OnClickListener { v ->
        when(v.id) {
            R.id.btn_lock_close -> finish()

            R.id.btn_check_all -> setChkAll()

            R.id.btn_app_setting -> setMultiChkSetting()
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

    /**
     * 멀티 선택에 대한 설정
     */
    private fun setMultiChkSetting() {
        SELECTED_APP = listAdapter_all.getCheckedAll()

        if(SELECTED_APP == 0) {
            Toast.makeText(applicationContext, R.string.msg_no_selected, Toast.LENGTH_SHORT).show()
            return
        }

        // dialog parameter
        SELECTED_METHOD = SELECTED_INDEX.toString()
        OPTION1 = setting.getMode()
        OPTION2 = "N"
        PKGIMG = resources.getDrawable(R.drawable.main_icon)
        PKGID = ""
        PKGNAME = ""
        LMTTIME = ""
        ISCHECKEDAPP = true

        val dialog = SkySettingDialog(this, PKGIMG, PKGNAME, PKGID, LMTTIME, SELECTED_METHOD,
            OPTION1, OPTION2, ISCHECKEDAPP, SELECTED_APP)
        dialog.setOnDismissListener(onDismissListener)
        dialog.show()
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

        if((gbn == null && word == null) || gbn == "ALL") {
            listAdapter_all = SkyAppListAdapter(this, adapterList_all, SELECTED_INDEX)
            mListView_All.adapter = listAdapter_all

            return
        }

        val ADAPTERLIST_FILTER = ArrayList<SkyAppListInfo>()

        for(i in 0 until adapterList_all.size) {
            if(adapterList_all[i].APP_NAME == null || adapterList_all[i].APP_NAME.toString().trim() == "") continue

            val row = if(word != null && word != "")
                adapterList_all[i].APP_NAME.toLowerCase().indexOf(word.toLowerCase())
            else
                if(adapterList_all[i].APP_GBN == gbn) 1 else -1

            if(row > -1) {
                ADAPTERLIST_FILTER.add(adapterList_all[i])
            }
        }

        listAdapter_all = SkyAppListAdapter(this, ADAPTERLIST_FILTER, SELECTED_INDEX)
        mListView_All.adapter = listAdapter_all
    }

    val onDismissListener_self = OnDismissListener {
        initApp()
    }

    val onDismissListener = OnDismissListener { dialog ->
        val settingDialog = dialog as SkySettingDialog

        type = settingDialog.getType()
        common.setLogMsg("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ $type")

        when(settingDialog.getType()) {
            // 취소
            0 -> {
                lmttime = 0
                common.setLogMsg("취소")
            }

            // 시간 설정
            1 -> {
                lmttime = settingDialog.getTime() * 60 * 1000
                option1 = settingDialog.getOption1()
                option2 = settingDialog.getOption2()
                common.setLogMsg("타이머 설정 시작")
                setSaveAppList(false)
            }

            // App Lock
            2 -> {
                lmttime = 0
                option1 = settingDialog.getOption1()
                option2 = settingDialog.getOption2()
                common.setLogMsg("앱락 설정 시작")
                setSaveAppList(false)
            }

            // 삭제
            3 -> {
                lmttime = 0
                setSaveAppList(true)
            }
        }
    }

    /**
     * 입력 및 업데이트
     */
    @Synchronized
    private fun setSaveAppList(isDelete: Boolean) {
        var cnt = 0

        ProgressTask(this).execute(listAdapter_all.getChecked().size)

        for(row in 0 until listAdapter_all.count) {
            if(listAdapter_all.getChecked(row)) {
                listAdapter_all.setUnChecked(row)

                // 삭제
                if(isDelete) {
                    setDeleteAppList(row)
                }
                // 저장
                else {
                    // 업데이트 로직
                    val pkg_name = listAdapter_all.getPkgName(row)
                    cnt = 0
                    if(::db_select_list.isInitialized) {
                        for(i in 0 until db_select_list.size) {
                            if(db_select_list[i]["pkgnme"].toString().trim().equals(pkg_name, ignoreCase = true)) {
                                cnt++
                                break
                            }
                        }
                    }

                    // 업데이트
                    if(cnt > 0)
                        setUpdateAppList(row)
                    // 신규 입력
                    else {
                        val hm = HashMap<Object, Object>()
                        hm["pkgnme"] = pkg_name as Object
                        hm["exe_gbn"] = type as Object
                        hm["lmttime"] = lmttime as Object
                        hm["option1"] = option1 as Object
                        hm["option2"] = option2 as Object

                        setInsertAppList(row)

                        db_select_list.add(hm)
                    }
                }
            }
        }

        listAdapter_all.notifyDataSetChanged()
        getAppList()
    }

    /**
     * 선택된 앱 리스트 저장
     */
    @SuppressLint("SimpleDateFormat")
    @Synchronized
    private fun setInsertAppList(row: Int) {
        val values = HashMap<Object, Object>()

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        val dTime = formatter.format(currentTime)
        var packagename = ""

        try {
            packagename = (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_PKG_NAME
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_GBN = type.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_LMTTIME = lmttime.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_SPCTIME = lmttime.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION1 = option1
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION2 = option2

            values["pkgnme"] = packagename as Object
            values["exe_gbn"] = type as Object
            values["app_stdte"] = dTime as Object
            values["lmttime"] = lmttime as Object // 밀리세컨드 1분
            values["spctime"] = lmttime as Object // 밀리세컨드 1분
            values["option1"] = option1 as Object
            values["option2"] = option2 as Object

            common.setLogMsg("설정 추가 $packagename, exe_gbn=$type, app_stdte=$dTime, lmttime=$lmttime, option1=$option1, option2=$option2")

            dbm.executeInsert("applist", values)
        } catch(e: Exception) {
            common.setLogMsg("저장 오류 :: $e")
            return
        }
    }

    /**
     * 수정
     */
    @SuppressLint("SimpleDateFormat")
    @Synchronized
    private fun setUpdateAppList(row: Int) {
        val values = HashMap<Object, Object>()

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        val dTime = formatter.format(currentTime)
        var packagename = ""
        val whereClause = "pkgnme=?"

        try {
            val whereArgs = arrayOf((mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_PKG_NAME)
            packagename = (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_PKG_NAME
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_LMTTIME = lmttime.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_SPCTIME = lmttime.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_GBN = type.toString()
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION1 = option1
            (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION2 = option2

            values["pkgnme"] = packagename as Object
            values["exe_gbn"] = type as Object
            values["app_stdte"] = dTime as Object
            values["lmttime"] = lmttime as Object // 밀리세컨드 1분
            values["spctime"] = lmttime as Object // 밀리세컨드 1분
            values["option1"] = option1 as Object
            values["option2"] = option2 as Object

            common.setLogMsg("설정 수정 $packagename, exe_gbn=$type, app_stdte=$dTime, lmttime=$lmttime, option1=$option1, option2=$option2")

            dbm.executeUpdate("applist", values, whereClause, whereArgs)
        } catch(e: Exception) {
            common.setLogMsg("수정 오류 :: $e")
            return
        }
    }

    /**
     * 삭제
     */
    @Synchronized
    private fun setDeleteAppList(row: Int) {
        val whereClause = "pkgnme=?"
        val pkg_name = (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_PKG_NAME.trim()

        (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_GBN = "0"
        (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION1 = option1
        (mListView_All.adapter.getItem(row) as SkyAppListInfo).APP_OPTION2 = "N"

        val whereArgs = arrayOf(pkg_name)

        try {
            if(::db_select_list.isInitialized) {
                for(i in 0 until db_select_list.size) {
                    if(db_select_list[i]["pkgnme"].toString().trim().equals(pkg_name, ignoreCase = true)) {
                        db_select_list.removeAt(i)
                        break
                    }
                }
            }

            dbm.executeDelete("applist", whereClause, whereArgs)
        } catch(e: Exception) {
            common.setLogMsg("삭제 오류 :: $e")
            return
        }
    }

    /**
     * 키보드 숨기기
     */
    private fun closeKeyPad() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edt_search_app.windowToken, 0)
        } catch(e: Exception) {
            common.setLogMsg("키보드 숨기기 오류 : $e")
        }
    }

    private var progressDialog: ProgressDialog? = null

    /**
     * Progress 보이기
     */
    protected fun showWaitProg() {
        showWaitProg(null)
    }

    /**
     * Progress 보이기
     * @param msg
     */
    protected fun showWaitProg(msg: String?) {
        progressDialog = ProgressDialog.show(this, "", if(msg == null || msg == "") "Please Wait..." else msg, true, true)
    }

    /**
     * Progress 숨기기
     */
    protected fun hideWaitProg() {
        progressDialog?.dismiss()
    }
}