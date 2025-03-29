package com.skynet.skytimelock.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.skytimelock.bean.SkyAppListInfo
import com.skynet.skytimelock.bean.SkyProgListInfo
import com.skynet.skytimelock.free.R
import com.skynet.skytimelock.view.SkySettingDialog
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap

class SkyProgListAdapter(private var ctx: Context?, private var infoList: ArrayList<SkyProgListInfo>?) : BaseAdapter() {
    private var inflater: LayoutInflater? = null
    private var viewHolder: ViewHolder? = null

    private lateinit var dbm: DbManager
    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting

    private var option1: String = ""
    private var option2: String = ""

    private var edt_search_app: EditText? = null

    // 저장될 데이터를 위한 변수
    private var clickedRow: Int = 0 // 선택된 row num
    /**
     * 1:시간 설정, 2:잠금앱
     */
    private var type: Int = 0       // 저장된 타입
    private var lmttime: Long = 0   // 설정한 시간(밀리세컨드)

    init {
        ctx?.let {
            inflater = LayoutInflater.from(it)
            common = SkyTimeLockCommon(it)
            setting = common.getSetting("SkyAppListAdapter")
            dbm = DbManager(it)
        }
    }

    // Adapter가 관리할 Data의 개수를 설정
    override fun getCount(): Int {
        return infoList?.size ?: 0
    }

    // Adapter가 관리하는 Data의 Item의 Position을 <객체> 형태로 얻어옴
    override fun getItem(position: Int): SkyProgListInfo {
        return infoList!![position]
    }

    // Adapter가 관리하는 Data의 Item의 position 값의 ID를 얻어옴
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getPkgName(row: Int): String {
        return getItem(row).getAPP_PKG_NAME()
    }

    // ListView에 표시될 한 줄의 Row를 설정
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater?.inflate(R.layout.row_prog_list_view, null)

            v?.let {
                viewHolder?.layout_row_app = it.findViewById(R.id.layout_row_app)
                viewHolder?.row_app_body = it.findViewById(R.id.row_app_body)
                viewHolder?.lay_prog_frame = it.findViewById(R.id.lay_prog_frame)
                viewHolder?.lay_prog = it.findViewById(R.id.lay_prog)

                viewHolder?.txt_app_name = it.findViewById(R.id.txt_app_name)
                viewHolder?.txt_rate = it.findViewById(R.id.txt_rate)
                viewHolder?.txt_used_time = it.findViewById(R.id.txt_used_time)
                viewHolder?.txt_run = it.findViewById(R.id.txt_run)

                viewHolder?.img_app_icon = it.findViewById(R.id.img_app_icon)

                it.tag = viewHolder
            }
        } else {
            viewHolder = v.tag as ViewHolder
        }

        // 앱 정보 설정
        viewHolder?.img_app_icon?.setImageDrawable(getItem(position).getAPP_ICON())
        viewHolder?.txt_app_name?.text = getItem(position).getAPP_NAME()
        viewHolder?.lay_prog?.layoutParams = LinearLayout.LayoutParams(0, 30, getItem(position).getAPP_RATE())

        // 프로그레스 바와 실행 횟수 표시 설정
        if (getItem(position).getAPP_RATE() == 0f) {
            viewHolder?.lay_prog_frame?.visibility = View.GONE
            viewHolder?.txt_run?.visibility = View.GONE
        } else {
            viewHolder?.lay_prog_frame?.visibility = View.VISIBLE
            viewHolder?.txt_run?.visibility = View.VISIBLE
            viewHolder?.txt_run?.text = "run : ${getItem(position).getAPP_RUN()}"
        }

        viewHolder?.txt_rate?.text = "${getItem(position).getAPP_RATE()}%"
        viewHolder?.txt_used_time?.text = getItem(position).getAPP_USE_TIME()

        viewHolder?.row_app_body?.tag = position
        viewHolder?.row_app_body?.setOnClickListener(clickListener)

        return v!!
    }

    // Adapter가 관리하는 Data List를 교체
    fun setArrayList(arrays: ArrayList<SkyProgListInfo>) {
        this.infoList = arrays
    }

    fun getArrayList(): ArrayList<SkyProgListInfo>? {
        return infoList
    }

    // ViewHolder 클래스
    private class ViewHolder {
        var layout_row_app: LinearLayout? = null
        var row_app_body: LinearLayout? = null
        var lay_prog_frame: LinearLayout? = null
        var lay_prog: LinearLayout? = null

        var txt_app_name: TextView? = null
        var txt_rate: TextView? = null
        var txt_used_time: TextView? = null
        var txt_run: TextView? = null

        var img_app_icon: ImageView? = null
    }

    private val clickListener = OnClickListener { v ->
        clickedRow = v.tag.toString().toInt()

        when (v.id) {
            R.id.row_app_body -> {
                val ISCHECKEDAPP = false

                val SELECTED_METHOD = "2"
                val OPTION1 = setting.getMode()
                val OPTION2 = "N"
                val PKGIMG = getItem(clickedRow).getAPP_ICON()
                val PKGID = getItem(clickedRow).getAPP_PKG_NAME()
                val PKGNAME = getItem(clickedRow).getAPP_NAME()
                val LMTTIME = "10"

                val dialog = SkySettingDialog(ctx, PKGIMG, PKGNAME, PKGID, LMTTIME, SELECTED_METHOD, OPTION1, OPTION2, ISCHECKEDAPP, 1)
                dialog.setOnDismissListener(onDismissListener)
                dialog.show()
            }
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        free()
        super.finalize()
    }

    private fun free() {
        inflater = null
        infoList = null
        viewHolder = null
        ctx = null
    }

    /**
     * 다이얼로그 종료 후 설정된 값 받아오기
     */
    val onDismissListener = OnDismissListener { dialog ->
        val settingDialog = dialog as SkySettingDialog

        type = settingDialog.getType()

        // 서비스 시작
        if (settingDialog.getType() > 0) {
            common.StartService()
        }

        when (settingDialog.getType()) {
            // 취소
            0 -> lmttime = 0
            // 시간 설정
            1 -> {
                lmttime = settingDialog.getTime() * 60 * 1000
                option1 = settingDialog.getOption1()
                option2 = settingDialog.getOption2()
                setSaveAppList()
            }
            // App Lock
            2 -> {
                lmttime = 0
                option1 = settingDialog.getOption1()
                option2 = settingDialog.getOption2()
                setSaveAppList()
            }
            // 설정 취소 (삭제)
            3 -> {
                lmttime = 0
                setDeleteAppList()
            }
        }
    }

    /**
     * 입력 및 업데이트
     */
    private fun setSaveAppList() {
        val pkg_name = getItem(clickedRow).getAPP_PKG_NAME().trim()

        val select_list = dbm.executeSelect("select * from applist where pkgnme = ?", arrayOf(pkg_name))

        if (select_list != null && select_list.isNotEmpty()) {
            setUpdateAppList()
        } else {
            val hm = HashMap<Any, Any>()
            hm["pkgnme"] = pkg_name
            hm["exe_gbn"] = type
            hm["lmttime"] = lmttime
            hm["option1"] = option1
            hm["option2"] = option2
            setInsertAppList()
        }
    }

    /**
     * 선택된 앱 리스트 저장
     */
    @SuppressLint("SimpleDateFormat")
    private fun setInsertAppList() {
        val values = HashMap<Any, Any>()

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        val dTime = formatter.format(currentTime)

        try {
            values["pkgnme"] = getItem(clickedRow).getAPP_PKG_NAME()
            values["exe_gbn"] = type
            values["app_stdte"] = dTime
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = lmttime // 밀리세컨드 1분
            values["option1"] = option1
            values["option2"] = option2

            common.setLogMsg("설정 추가 type=$type, option1=$option1, option2=$option2")

            dbm.executeInsert("applist", values)
        } catch (e: Exception) {
            ctx?.let {
                Toast.makeText(it, it.getString(R.string.save_err), Toast.LENGTH_SHORT).show()
            }
            common.setLogMsg("설정 추가 오류 :: $e")
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

        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val currentTime = Date()
        val dTime = formatter.format(currentTime)

        val whereClause = "pkgnme=?"

        try {
            val whereArgs = arrayOf(getItem(clickedRow).getAPP_PKG_NAME())

            values["pkgnme"] = getItem(clickedRow).getAPP_PKG_NAME()
            values["exe_gbn"] = type
            values["app_stdte"] = dTime
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = lmttime // 밀리세컨드 1분
            values["option1"] = option1
            values["option2"] = option2

            common.setLogMsg("설정 수정 pkg=${getItem(clickedRow).getAPP_PKG_NAME()}, type=$type, option1=$option1, option2=$option2")

            dbm.executeUpdate("applist", values, whereClause, whereArgs)
        } catch (e: Exception) {
            ctx?.let {
                Toast.makeText(it, it.getString(R.string.update_err), Toast.LENGTH_SHORT).show()
            }
            common.setLogMsg("설정 수정 오류 :: $e")
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
        val whereClause = "pkgnme=?"
        val pkg_name = getItem(clickedRow).getAPP_PKG_NAME().trim()

        val whereArgs = arrayOf(pkg_name)

        try {
            infoList?.let { list ->
                for (i in list.indices) {
                    if (list[i].getAPP_PKG_NAME().trim().equals(pkg_name, ignoreCase = true)) {
                        list.removeAt(i)
                        break
                    }
                }
            }

            dbm.executeDelete("applist", whereClause, whereArgs)

            notifyDataSetChanged()
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

    /**
     * 구분 오름차순
     */
    class GbnAscCompare : Comparator<SkyAppListInfo> {
        override fun compare(arg0: SkyAppListInfo, arg1: SkyAppListInfo): Int {
            return arg0.getAPP_GBN().compareTo(arg1.getAPP_GBN())
        }
    }

    /**
     * 구분 내림차순
     */
    class GbnDescCompare : Comparator<SkyAppListInfo> {
        override fun compare(arg0: SkyAppListInfo, arg1: SkyAppListInfo): Int {
            return arg1.getAPP_GBN().compareTo(arg0.getAPP_GBN())
        }
    }
}