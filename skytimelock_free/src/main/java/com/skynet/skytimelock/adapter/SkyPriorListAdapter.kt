package com.skynet.skytimelock.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.skytimelock.bean.SkyPriorListInfo
import com.skynet.skytimelock.free.R
import com.skynet.skytimelock.view.SkyPriorList
import com.skynet.skytimelock.view.SkyPriorSettingDialog
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

class SkyPriorListAdapter(private var ctx: Context?, private var infoList: ArrayList<SkyPriorListInfo>) : BaseAdapter() {
    private var inflater: LayoutInflater? = null
    private var viewHolder: ViewHolder? = null
    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting

    // 저장될 데이터를 위한 변수들
    private var clickedRow: Int = 0 // 선택된 row num
    private var type: Int = 0       // 저장된 타입
    private var lmttime: Long = 0   // 설정한 시간(밀리세컨드)
    private var spctime: Long = 0
    private var dayweek: String = ""

    init {
        ctx?.let {
            inflater = LayoutInflater.from(it)
            common = SkyTimeLockCommon(it)
            setting = common.getSetting("SkyPriorListAdapter")
        }
    }

    // Adapter가 관리할 Data의 개수를 설정
    override fun getCount(): Int {
        return infoList.size
    }

    // Adapter가 관리하는 Data의 Item의 Position을 <객체> 형태로 얻어옴
    override fun getItem(position: Int): SkyPriorListInfo {
        return infoList[position]
    }

    // Adapter가 관리하는 Data의 Item 의 position 값의 ID를 얻어옴
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // ViewHolder 클래스
    private class ViewHolder {
        var layout_row_app: LinearLayout? = null
        var mark_sun: LinearLayout? = null
        var mark_mon: LinearLayout? = null
        var mark_tue: LinearLayout? = null
        var mark_wed: LinearLayout? = null
        var mark_thu: LinearLayout? = null
        var mark_fri: LinearLayout? = null
        var mark_sat: LinearLayout? = null
        var btn_prior_delete: Button? = null
        var btn_prior_setting: Button? = null

        var txt_prior_lmttime: TextView? = null
        var txt_prior_spctime: TextView? = null
    }

    // ListView에 표시될 한 줄의 Row를 설정
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater?.inflate(R.layout.row_prior_list_view, null)

            v?.let {
                viewHolder?.layout_row_app = it.findViewById(R.id.layout_row_app)
                viewHolder?.mark_sun = it.findViewById(R.id.mark_sun)
                viewHolder?.mark_mon = it.findViewById(R.id.mark_mon)
                viewHolder?.mark_tue = it.findViewById(R.id.mark_tue)
                viewHolder?.mark_wed = it.findViewById(R.id.mark_wed)
                viewHolder?.mark_thu = it.findViewById(R.id.mark_thu)
                viewHolder?.mark_fri = it.findViewById(R.id.mark_fri)
                viewHolder?.mark_sat = it.findViewById(R.id.mark_sat)
                viewHolder?.btn_prior_delete = it.findViewById(R.id.btn_prior_delete)
                viewHolder?.btn_prior_setting = it.findViewById(R.id.btn_prior_setting)

                viewHolder?.txt_prior_lmttime = it.findViewById(R.id.txt_prior_lmttime)
                viewHolder?.txt_prior_spctime = it.findViewById(R.id.txt_prior_spctime)

                it.tag = viewHolder
            }
        } else {
            viewHolder = v.tag as ViewHolder
        }

        val lmttime = getItem(position).getLmttime().toLong()
        val spctime = getItem(position).getSpctime().toLong()

        viewHolder?.txt_prior_lmttime?.text = getTime(lmttime)
        viewHolder?.txt_prior_spctime?.text = getTime(spctime)

        val dayweek = getItem(position).getDayweek().split("|")

        val act = Color.GREEN
        val def = Color.LTGRAY

        viewHolder?.mark_sun?.setBackgroundColor(if (dayweek[1] == "1") act else def)
        viewHolder?.mark_mon?.setBackgroundColor(if (dayweek[3] == "1") act else def)
        viewHolder?.mark_tue?.setBackgroundColor(if (dayweek[5] == "1") act else def)
        viewHolder?.mark_wed?.setBackgroundColor(if (dayweek[7] == "1") act else def)
        viewHolder?.mark_thu?.setBackgroundColor(if (dayweek[9] == "1") act else def)
        viewHolder?.mark_fri?.setBackgroundColor(if (dayweek[11] == "1") act else def)
        viewHolder?.mark_sat?.setBackgroundColor(if (dayweek[13] == "1") act else def)

        viewHolder?.btn_prior_setting?.setOnClickListener(clickListener)
        viewHolder?.btn_prior_delete?.setOnClickListener(clickListener)
        viewHolder?.layout_row_app?.setOnClickListener(clickListener)

        viewHolder?.layout_row_app?.tag = position
        viewHolder?.btn_prior_delete?.tag = position
        viewHolder?.btn_prior_setting?.tag = position

        return v!!
    }

    // 시간 문자열로 변환
    private fun getTime(mtime: Long): String {
        val hour = (mtime / (1000 * 60 * 60)) % 24
        val min = (mtime / (1000 * 60)) % 60

        return "${if (hour < 10) "0$hour" else hour}:${if (min < 10) "0$min" else min}"
    }

    // Adapter가 관리하는 Data List를 교체
    fun setArrayList(arrays: ArrayList<SkyPriorListInfo>) {
        this.infoList = arrays
    }

    fun getArrayList(): ArrayList<SkyPriorListInfo> {
        return infoList
    }

    // 클릭 리스너
    private val clickListener = View.OnClickListener { v ->
        clickedRow = v.tag.toString().toInt()

        if (v.id == R.id.btn_prior_delete) {
            setDeleteAppList()
        } else {
            val param_dayweek = getItem(clickedRow).getDayweek()
            val lmttime = getItem(clickedRow).getLmttime().toLong()
            val spctime = getItem(clickedRow).getSpctime().toLong()

            val dialog = SkyPriorSettingDialog(ctx, param_dayweek, lmttime, spctime)
            dialog.setOnDismissListener(onDismissListener)
            dialog.show()
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        free()
        super.finalize()
    }

    private fun free() {
        inflater = null
        infoList.clear()
        viewHolder = null
        ctx = null
    }

    /**
     * 다이얼로그 종료 후 설정된 값 받아오기
     */
    val onDismissListener = OnDismissListener { dialog ->
        val priorSettingDialog = dialog as SkyPriorSettingDialog

        type = priorSettingDialog.getType()

        // 서비스 시작
        if (priorSettingDialog.getType() > 0) {
            common.setRefreshSetting(true)
        }

        when (priorSettingDialog.getType()) {
            // 취소
            0 -> lmttime = 0
            // 시간 설정
            1 -> {
                lmttime = priorSettingDialog.getLmttime()
                spctime = priorSettingDialog.getSpctime()
                dayweek = priorSettingDialog.getDayweek()
                setSaveAppList()
            }
        }

        common.setRefreshSetting(true)
    }

    /**
     * 현재 선택한 row index
     */
    fun getPosition(): Int {
        return this.clickedRow
    }

    /**
     * 오늘 날짜 반환 yyyy-MM-dd
     */
    @SuppressLint("SimpleDateFormat")
    private fun getToDay(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        return formatter.format(currentTime)
    }

    /**
     * 입력 및 업데이트
     */
    private fun setSaveAppList() {
        var cnt = 0

        if (dayweek.isEmpty() || dayweek == "0|0|0|0|0|0|0|") return

        // db에 저장된 내용이 없으면 insert
        if (SkyPriorList.db_prior_list == null || SkyPriorList.db_prior_list.isEmpty()) {
            setInsertAppList()
            return
        }

        for (i in 0 until SkyPriorList.db_prior_list.size) {
            if (SkyPriorList.db_prior_list[i]["dayweek"].toString().trim().equals(dayweek, ignoreCase = true)) {
                cnt++
                break
            }
        }

        if (cnt > 0) {
            setUpdateAppList()
        } else {
            val hm = HashMap<Any, Any>()
            hm["dayweek"] = dayweek
            hm["lmttime"] = lmttime
            hm["spctime"] = spctime // 밀리세컨드 1분
            hm["exe_eddte"] = getToDay()
            SkyPriorList.db_prior_list.add(hm)

            setInsertAppList()
        }
    }

    /**
     * 추가
     */
    private fun setInsertAppList() {
        val values = HashMap<Any, Any>()

        try {
            getItem(clickedRow).setDayweek(getItem(clickedRow).getDayweek())
            getItem(clickedRow).setLmttime("$lmttime")
            getItem(clickedRow).setSpctime("$lmttime")
            SkyPriorList.listAdapter_all.notifyDataSetChanged()

            values["dayweek"] = dayweek
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = spctime // 밀리세컨드 1분
            values["exe_eddte"] = getToDay()

            SkyPriorList.dbm.executeInsert("priorlist", values)

            common.setRefreshSetting(true)
        } catch (e: Exception) {
            ctx?.let {
                Toast.makeText(it, it.getString(R.string.save_err), Toast.LENGTH_SHORT).show()
            }
            common.setLogMsg("저장 오류 :: $e")
            return
        }

        ctx?.let {
            Toast.makeText(it, it.getString(R.string.save_ok), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 수정
     */
    @SuppressLint("SimpleDateFormat")
    private fun setUpdateAppList() {
        val values = HashMap<Any, Any>()

        val whereClause = "dayweek=?"

        try {
            val whereArgs = arrayOf(dayweek)

            getItem(clickedRow).setDayweek(dayweek)
            getItem(clickedRow).setLmttime("$lmttime")
            getItem(clickedRow).setSpctime("$lmttime")
            SkyPriorList.listAdapter_all.notifyDataSetChanged()

            values["dayweek"] = getItem(clickedRow).getDayweek()
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = spctime // 밀리세컨드 1분
            values["exe_eddte"] = getToDay()

            SkyPriorList.dbm.executeUpdate("priorlist", values, whereClause, whereArgs)

            common.setRefreshSetting(true)
        } catch (e: Exception) {
            ctx?.let {
                Toast.makeText(it, it.getString(R.string.update_err), Toast.LENGTH_SHORT).show()
            }
            common.setLogMsg("수정 오류 :: $e")
            return
        }

        ctx?.let {
            Toast.makeText(it, it.getString(R.string.update_ok), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 삭제
     */
    private fun setDeleteAppList() {
        val pdayweek = getItem(clickedRow).getDayweek()
        val whereClause = "dayweek=?"
        val whereArgs = arrayOf(pdayweek)

        try {
            for (i in 0 until SkyPriorList.db_prior_list.size) {
                if (SkyPriorList.db_prior_list[i]["dayweek"].toString().trim().equals(pdayweek, ignoreCase = true)) {
                    SkyPriorList.db_prior_list.removeAt(i)
                    SkyPriorList.adapterList_all.removeAt(i)
                    break
                }
            }

            SkyPriorList.dbm.executeDelete("priorlist", whereClause, whereArgs)
            common.setRefreshSetting(true)

            SkyPriorList.listAdapter_all.notifyDataSetChanged()
        } catch (e: Exception) {
            ctx?.let {
                Toast.makeText(it, it.getString(R.string.delete_err), Toast.LENGTH_SHORT).show()
            }
            common.setLogMsg("삭제 오류 :: $e")
            return
        }

        ctx?.let {
            Toast.makeText(it, it.getString(R.string.delete_ok), Toast.LENGTH_SHORT).show()
        }
    }
}