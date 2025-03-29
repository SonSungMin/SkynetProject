package com.skynet.skytimelock.view

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ListView

import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.adapter.SkyPriorListAdapter
import com.skynet.skytimelock.bean.SkyPriorListInfo
import com.skynet.skytimelock.free.R

@SuppressLint("SimpleDateFormat")
class SkyPriorList : SkyNetBaseActivity() {
    companion object {
        lateinit var adapterList_all: ArrayList<SkyPriorListInfo>
        lateinit var listAdapter_all: SkyPriorListAdapter
        lateinit var db_prior_list: ArrayList<HashMap<Object, Object>>
        lateinit var dbm: DbManager
    }

    private lateinit var mListView_All: ListView

    private var lmttime: Long = 0
    private var spctime: Long = 0
    private lateinit var dayweek: String

    private lateinit var setting: SkyTimeLockSetting

    private lateinit var btn_prior_add_setting: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(false)
        super.setLayout_Id(R.layout.list_prior)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dbm = DbManager(this.applicationContext)
        setting = common.getSetting("SkyPriorList")

        btn_prior_add_setting = findViewById(R.id.btn_prior_add_setting)
        btn_prior_add_setting.setOnClickListener(btn_clickEvent)

        getAllAppList()
    }

    private val btn_clickEvent = OnClickListener { v ->
        when (v.id) {
            R.id.btn_prior_add_setting -> {
                val dialog = SkyPriorSettingDialog(this@SkyPriorList, "0|0|0|0|0|0|0|", 60 * 60 * 1000, 60 * 60 * 1000)
                dialog.setOnCancelListener(onDismissListener)
                dialog.show()
            }
        }
    }

    private fun getAllAppList() {
        adapterList_all = ArrayList()
        db_prior_list = ArrayList()

        try {
            db_prior_list = dbm.executeSelect("SELECT * FROM priorlist", null)
        } catch (e: Exception) {
            SetLog("getAllAppList :: $e")
            return
        }

        if (db_prior_list.isEmpty()) return

        for (i in 0 until db_prior_list.size) {
            val sInfo = SkyPriorListInfo().apply {
                setDayweek(db_prior_list[i]["dayweek"].toString())
                setLmttime(db_prior_list[i]["lmttime"].toString())
                setSpctime(db_prior_list[i]["spctime"].toString())
                setExe_eddte(db_prior_list[i]["exe_eddte"].toString())
            }

            adapterList_all.add(sInfo)
        }

        mListView_All = findViewById(R.id.grd_prior_list)
        listAdapter_all = SkyPriorListAdapter(this, adapterList_all)
        mListView_All.adapter = listAdapter_all
    }

    val onDismissListener = OnCancelListener { dialog ->
        val settingDialog = dialog as SkyPriorSettingDialog

        when (settingDialog.getType()) {
            // 취소
            0 -> {
                lmttime = 0
                SetLog("취소")
            }

            // 시간 설정
            1 -> {
                lmttime = settingDialog.getLmttime()
                spctime = settingDialog.getSpctime()
                dayweek = settingDialog.getDayweek()
                setInsertAppList()
            }
        }

        common.setLogMsg("${this.javaClass.name} = $dayweek")
        common.setRefreshSetting(true)
    }

    /**
     * 선택된 앱 리스트 추가
     */
    @SuppressLint("SimpleDateFormat")
    private fun setInsertAppList() {
        if (dayweek == "" || dayweek == "0|0|0|0|0|0|0|") return

        val values = HashMap<Object, Object>()

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        val dTime = formatter.format(currentTime)

        try {
            values["dayweek"] = dayweek as Object
            values["lmttime"] = lmttime as Object // 밀리세컨드 1분
            values["spctime"] = spctime as Object // 밀리세컨드 1분
            values["exe_eddte"] = dTime as Object

            val sInfo = SkyPriorListInfo().apply {
                setDayweek(dayweek)
                setLmttime(lmttime.toString())
                setSpctime(spctime.toString())
                setExe_eddte(dTime)
            }

            adapterList_all.add(sInfo)
            db_prior_list.add(values)

            mListView_All = findViewById(R.id.grd_prior_list)
            listAdapter_all = SkyPriorListAdapter(this, adapterList_all)
            mListView_All.adapter = listAdapter_all

            dbm.executeInsert("priorlist", values)
        } catch (e: Exception) {
            common.setLogMsg("setInsertAppList 저장 오류 :: $e")
            return
        }
    }
}