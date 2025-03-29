package com.skynet.skytimelock.adapter

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.skytimelock.bean.SkyAppListInfo
import com.skynet.skytimelock.free.R
import com.skynet.skytimelock.view.SkySettingDialog

class SkyAppListAdapter(
    private val ctx: Context,
    private var infoList: ArrayList<SkyAppListInfo>?,
    /**
     * 2: 전체 리스트, 3: 시간제한, 4: 잠금, 9:허용앱
     */
    private val SELECTED_INDEX: Int
) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(ctx)
    private var viewHolder: ViewHolder? = null

    private val dbm: DbManager = DbManager(ctx)
    private val common: SkyTimeLockCommon = SkyTimeLockCommon(ctx)
    private val setting: SkyTimeLockSetting = common.getSetting("SkyAppListAdapter")

    private var option1: String? = null
    private var option2: String? = null

    private var edt_search_app: EditText? = null

    private var isCheckedConfrim: BooleanArray

    // 저장될 데이터를 위해
    private var clickedRow: Int = 0 // 선택된 row num
    /**
     * 1:시간 설정, 2:잠금앱
     */
    private var type: Int = 0       // 저장된 타입
    private var lmttime: Int = 0   // 설정한 시간(밀리세컨드)

    init {
        // ArrayList Size 만큼의 boolean 배열을 만든다.
        // CheckBox의 true/false를 구별 하기 위해
        isCheckedConfrim = infoList?.size?.let { BooleanArray(it) } ?: BooleanArray(0)
    }

    // CheckBox를 모두 선택하는 메서드
    fun setAllChecked(ischecked: Boolean) {
        val tempSize = isCheckedConfrim.size

        for (a in 0 until tempSize) {
            isCheckedConfrim[a] = ischecked
        }
    }

    fun setChecked(position: Int) {
        isCheckedConfrim[position] = !isCheckedConfrim[position]
    }

    fun setUnChecked(position: Int) {
        isCheckedConfrim[position] = false
    }

    /**
     * 선택된 전체 갯수
     * @return
     */
    fun getCheckedAll(): Int {
        var cnt = 0
        for (i in isCheckedConfrim.indices) {
            if (isCheckedConfrim[i])
                cnt++
        }
        return cnt
    }

    fun getChecked(): ArrayList<Int> {
        val tempSize = isCheckedConfrim.size
        val mArrayList = ArrayList<Int>()
        for (b in 0 until tempSize) {
            if (isCheckedConfrim[b])
                mArrayList.add(b)
        }
        return mArrayList
    }

    fun getChecked(row: Int): Boolean {
        return isCheckedConfrim[row]
    }

    // Adapter가 관리할 Data의 개수를 설정 합니다.
    override fun getCount(): Int {
        return infoList?.size ?: 0
    }

    // Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
    override fun getItem(position: Int): SkyAppListInfo {
        return infoList!![position]
    }

    // Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getPkgName(row: Int): String? {
        return getItem(row).getAPP_PKG_NAME()
    }

    fun setCheck(row: Int, value: Boolean) {
        getItem(row).setAPP_CHK(value)
    }

    // ListView의 뿌려질 한줄의 Row를 설정 합니다.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var time: String?
        var spctime: String?
        var option1: String
        var option2: String

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater.inflate(R.layout.row_app_list_view, null)

            viewHolder?.layout_row_app = v.findViewById(R.id.layout_row_app)
            viewHolder?.row_app_body = v.findViewById(R.id.row_app_body)
            viewHolder?.check_app = v.findViewById(R.id.chk_app)
            viewHolder?.layout_row_empty = v.findViewById(R.id.layout_row_empty)

            viewHolder?.row_app_title = v.findViewById(R.id.row_app_title)
            viewHolder?.row_app_title_text = v.findViewById(R.id.row_app_title_text)

            viewHolder?.name = v.findViewById(R.id.txt_app_name)
            viewHolder?.pkgnme = v.findViewById(R.id.txt_app_pkgname)
            viewHolder?.desc = v.findViewById(R.id.txt_app_desc)
            viewHolder?.txt_app_desc = v.findViewById(R.id.txt_app_desc)
            viewHolder?.lmttime = v.findViewById(R.id.txt_app_lmttime)
            viewHolder?.spctime = v.findViewById(R.id.txt_app_spctime)
            viewHolder?.icon = v.findViewById(R.id.img_app_icon)
            viewHolder?.status_icon = v.findViewById(R.id.img_ststus_icon)
            viewHolder?.lock_icon = v.findViewById(R.id.img_lock_icon)
            viewHolder?.wifi_icon = v.findViewById(R.id.img_wifi_icon)

            v.tag = viewHolder
        } else {
            viewHolder = v.tag as ViewHolder
        }

        // 등록된 어플이 없을 경우 표시
        viewHolder?.layout_row_empty?.visibility = View.GONE

        // 기능별 어플 구분 타이틀 표시
        viewHolder?.row_app_title?.visibility = View.VISIBLE
        viewHolder?.row_app_title_text?.visibility = View.VISIBLE

        // ################################################################ 타이틀 설정 :: 시작
        // 시간 제한 어플
        when {
            getItem(position).getAPP_GBN() == "1" ->
                viewHolder?.row_app_title_text?.text = ctx.getString(R.string.app_row_title5)
            // 잠금 어플
            getItem(position).getAPP_GBN() == "2" ->
                viewHolder?.row_app_title_text?.text = ctx.getString(R.string.app_row_title6)
            // 추천 필수 잠금 어플
            getItem(position).getAPP_REQUIRED() ->
                viewHolder?.row_app_title_text?.text = ctx.getString(R.string.app_row_title1)
            // 항상 실행 가능 어플
            getItem(position).getAPP_ALLOWED() ->
                viewHolder?.row_app_title_text?.text = ctx.getString(R.string.app_row_title4)
            // 미설정 어플
            else ->
                viewHolder?.row_app_title_text?.text = ctx.getString(R.string.app_row_title3)
        }

        // 같은 종류의 타이틀 숨기기
        if (position > 0 && (getItem(position).getAPP_GBN() == getItem(position-1).getAPP_GBN())) {
            viewHolder?.row_app_title?.visibility = View.GONE
            viewHolder?.row_app_title_text?.visibility = View.GONE
        }

        // ################################################################ 타이틀 설정 :: 끝

        viewHolder?.row_app_title_text?.textSize = 15f

        viewHolder?.lock_icon?.visibility = View.VISIBLE
        viewHolder?.wifi_icon?.visibility = View.VISIBLE

        // 시간제한 또는 잠금인 경우 (설정 아이콘 표시)
        if (getItem(position).getAPP_GBN() == "1" || getItem(position).getAPP_GBN() == "2") {
            option1 = getItem(position).getAPP_OPTION1() ?: ""
            option2 = getItem(position).getAPP_OPTION2() ?: ""

            // 잠금 화면 아이콘 설정
            when (option1) {
                "1" -> viewHolder?.lock_icon?.setImageResource(R.drawable.icon_broken)
                "2" -> viewHolder?.lock_icon?.setImageResource(R.drawable.icon_number)
                "3" -> viewHolder?.lock_icon?.setImageResource(R.drawable.icon_pattern)
                else -> viewHolder?.lock_icon?.visibility = View.GONE
            }

            // wifi 아이콘 설정
            if (option2 == "Y") {
                viewHolder?.wifi_icon?.setImageResource(R.drawable.icon_wifi)
            } else {
                viewHolder?.wifi_icon?.setImageResource(R.drawable.icon_wifi_dis)
            }

            if (getItem(position).isISEMPTY()) {
                viewHolder?.layout_row_empty?.visibility = View.VISIBLE
                viewHolder?.layout_row_app?.visibility = View.GONE
            }
        } else {
            viewHolder?.lock_icon?.visibility = View.GONE
            viewHolder?.wifi_icon?.visibility = View.GONE
        }

        // 사용시간 제한인 경우
        if (getItem(position).getAPP_GBN() == "1") {
            time = getItem(position).getAPP_LMTTIME()
            spctime = getItem(position).getAPP_SPCTIME()

            time = if (time.isNullOrEmpty() || time == "null") "0" else time
            spctime = if (spctime.isNullOrEmpty() || spctime == "null") "0" else spctime

            viewHolder?.status_icon?.visibility = View.VISIBLE
            viewHolder?.status_icon?.setImageResource(R.drawable.icon_time)
            viewHolder?.txt_app_desc?.text = try {
                val settingMin = try {
                    Math.round(((time.toLong() / 60 / 1000).toDouble()))
                } catch (e: Exception) {
                    0
                }

                val spearMin = try {
                    Math.round(((spctime.toLong() / 60 / 1000).toDouble()))
                } catch (e: Exception) {
                    0
                }

                "${this.ctx.getString(R.string.txt_setting)} : ${settingMin}${this.ctx.getString(R.string.txt_min)} / ${this.ctx.getString(R.string.txt_spear)} : ${spearMin}${this.ctx.getString(R.string.txt_min)}"
            } catch (e: Exception) {
                Log.e("TAG", "Error formatting text: ${e.message}")
                this.ctx.getString(R.string.txt_setting) + ": 0" + this.ctx.getString(R.string.txt_min) + " / " +
                        this.ctx.getString(R.string.txt_spear) + ": 0" + this.ctx.getString(R.string.txt_min)
            }
        }
        // 잠금 어플인 경우
        else if (getItem(position).getAPP_GBN() == "2") {
            viewHolder?.status_icon?.visibility = View.VISIBLE
            viewHolder?.status_icon?.setImageResource(R.drawable.icon_lock)
            viewHolder?.txt_app_desc?.text = this.ctx.getString(R.string.txt_lockapp)
        } else {
            viewHolder?.status_icon?.visibility = View.GONE

            // 허용앱 등록이 아닌 경우만 클릭시 설정 팝업 띄우기
            if (this.SELECTED_INDEX == 9)
                viewHolder?.txt_app_desc?.text = ""
            else
                viewHolder?.txt_app_desc?.text = this.ctx.getString(R.string.txt_notset)

            viewHolder?.txt_app_desc?.setTextColor(Color.rgb(153, 153, 153))
            viewHolder?.name?.setTextColor(Color.rgb(0, 0, 0))
        }

        viewHolder?.name?.tag = position
        viewHolder?.name?.text = getItem(position).getAPP_NAME()
        viewHolder?.lmttime?.text = getItem(position).getAPP_LMTTIME()
        viewHolder?.pkgnme?.text = getItem(position).getAPP_PKG_NAME()
        viewHolder?.icon?.setImageDrawable(getItem(position).getAPP_ICON())

        viewHolder?.check_app?.isChecked = isCheckedConfrim[position]
        viewHolder?.check_app?.setOnClickListener(clickListener)
        viewHolder?.check_app?.tag = position

        viewHolder?.row_app_body?.tag = position
        // 허용앱 등록이 아닌 경우만 클릭시 설정 팝업 띄우기
        if (this.SELECTED_INDEX != 9 && !getItem(position).isISEMPTY())
            viewHolder?.row_app_body?.setOnClickListener(clickListener)

        return v!!
    }

    // Adapter가 관리하는 Data List를 교체 한다.
    // 교체 후 Adapter.notifyDataSetChanged() 메서드로 변경 사실을
    // Adapter에 알려 주어 ListView에 적용 되도록 한다.
    fun setArrayList(arrays: ArrayList<SkyAppListInfo>) {
        this.infoList = arrays
    }

    fun getArrayList(): ArrayList<SkyAppListInfo>? {
        return infoList
    }

    /** ViewHolder
     *  getView의 속도 향상을 위해 쓴다.
     *  한번의 findViewByID 로 재사용 하기 위해 viewHolder를 사용 한다.*/
    inner class ViewHolder {
        var layout_row_app: LinearLayout? = null
        var row_app_body: LinearLayout? = null

        var layout_row_empty: LinearLayout? = null

        var row_app_title: LinearLayout? = null
        var row_app_title_text: TextView? = null

        var name: TextView? = null
        var desc: TextView? = null
        var pkgnme: TextView? = null
        var txt_app_desc: TextView? = null
        var lmttime: TextView? = null
        var spctime: TextView? = null
        var icon: ImageView? = null
        var status_icon: ImageView? = null
        var lock_icon: ImageView? = null
        var wifi_icon: ImageView? = null
        var check_app: CheckBox? = null
    }

    private val clickListener = OnClickListener { v ->
        clickedRow = v.tag.toString().toInt()

        when (v.id) {
            R.id.chk_app -> setChecked(clickedRow)

            // 버튼 클릭
            // 버튼 클릭
            R.id.row_app_body -> {
                try {
                    val item = getItem(clickedRow)
                    if (item != null) {
                        val ISCHECKEDAPP = false
                        val SELECTED_METHOD = SELECTED_INDEX.toString()

                        // OPTION1 처리 시 안전한 방법 적용
                        val OPTION1 = try {
                            item.getAPP_OPTION1()
                        } catch (e: Exception) {
                            Log.e("TAG", "Error getting APP_OPTION1: ${e.message}")
                            setting.getMode() // 기본값으로 설정 모드 사용
                        } ?: setting.getMode()

                        val OPTION2 = try {
                            item.getAPP_OPTION2()
                        } catch (e: Exception) {
                            "N" // 기본값
                        } ?: "N"

                        val PKGIMG = item.getAPP_ICON() ?: ctx.resources.getDrawable(R.drawable.ic_default_app)
                        val PKGID = item.getAPP_PKG_NAME() ?: ""
                        val PKGNAME = item.getAPP_NAME() ?: ""
                        val LMTTIME = item.getAPP_LMTTIME() ?: "0"

                        val dialog = SkySettingDialog(ctx, PKGIMG, PKGNAME, PKGID, LMTTIME, SELECTED_METHOD, OPTION1, OPTION2, ISCHECKEDAPP, 1)
                        dialog.setOnDismissListener(onDismissListener)
                        dialog.show()
                    } else {
                        Log.e("TAG", "Item at position $clickedRow is null")
                        Toast.makeText(ctx, "앱 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Error opening settings dialog: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(ctx, "앱 설정을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 다이얼로그 종료 후 설정된 값 받아오기
     */
    val onDismissListener = OnDismissListener { dialog ->
        val settingDialog = dialog as SkySettingDialog

        type = settingDialog.getType()

        // 서비스 시작
        if (settingDialog.getType() > 0)
            common.startService()

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
            // 설정 취소
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
        var cnt = 0

        // db에 저장된 내용이 없으면 insert
        if (infoList == null || infoList!!.isEmpty()) {
            setInsertAppList()
            return
        }

        // 업데이트 로직
        val pkg_name = getItem(clickedRow).getAPP_PKG_NAME()?.trim()

        for (i in 0 until infoList!!.size) {
            if (infoList!![i].getAPP_PKG_NAME() != null &&
                infoList!![i].getAPP_PKG_NAME()?.trim().equals(pkg_name, ignoreCase = true) &&
                (infoList!![i].getAPP_GBN() == "1" || infoList!![i].getAPP_GBN() == "2")
            ) {
                cnt++
                break
            }
        }

        if (cnt > 0) {
            setUpdateAppList()
        } else {
            val hm = HashMap<Any, Any>()
            hm["pkgnme"] = pkg_name
            hm["exe_gbn"] = type
            hm["lmttime"] = lmttime
            hm["option1"] = option1.orEmpty()
            hm["option2"] = option2.orEmpty()
            setInsertAppList()
        }

        // 정렬
        Collections.sort(infoList, GbnDescCompare())
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
            getItem(clickedRow).setAPP_GBN(type.toString())
            getItem(clickedRow).setAPP_LMTTIME(lmttime.toString())
            getItem(clickedRow).setAPP_SPCTIME(lmttime.toString())
            getItem(clickedRow).setAPP_OPTION1(option1.orEmpty())
            getItem(clickedRow).setAPP_OPTION2(option2.orEmpty())

            notifyDataSetChanged()

            values["exe_gbn"] = type
            values["app_stdte"] = dTime
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = lmttime // 밀리세컨드 1분
            values["option1"] = option1.orEmpty()
            values["option2"] = option2.orEmpty()

            common.setLogMsg("설정 추가 type=$type, option1=$option1, option2=$option2")

            dbm.executeInsert("applist", values)
        } catch (e: Exception) {
            Toast.makeText(ctx, ctx.getString(R.string.save_err), Toast.LENGTH_SHORT).show()
            common.setLogMsg("설정 추가 오류 :: $e")
            return
        }
        Toast.makeText(ctx, ctx.getString(R.string.save_ok), Toast.LENGTH_SHORT).show()
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
        val whereArgs: Array<String>

        try {
            whereArgs = arrayOf(getItem(clickedRow).getAPP_PKG_NAME())

            values["pkgnme"] = getItem(clickedRow).getAPP_PKG_NAME()
            getItem(clickedRow).setAPP_LMTTIME(lmttime.toString())
            getItem(clickedRow).setAPP_SPCTIME(lmttime.toString())
            getItem(clickedRow).setAPP_GBN(type.toString())
            getItem(clickedRow).setAPP_OPTION1(option1.orEmpty())
            getItem(clickedRow).setAPP_OPTION2(option2.orEmpty())
            notifyDataSetChanged()

            values["exe_gbn"] = type
            values["app_stdte"] = dTime
            values["lmttime"] = lmttime // 밀리세컨드 1분
            values["spctime"] = lmttime // 밀리세컨드 1분
            values["option1"] = option1.orEmpty()
            values["option2"] = option2.orEmpty()

            common.setLogMsg("설정 수정 pkg=${getItem(clickedRow).getAPP_PKG_NAME()}, type=$type, option1=$option1, option2=$option2")

            dbm.executeUpdate("applist", values, whereClause, whereArgs)
        } catch (e: Exception) {
            Toast.makeText(ctx, ctx.getString(R.string.update_err), Toast.LENGTH_SHORT).show()
            common.setLogMsg("설정 수정 오류 :: $e")
            return
        }
        Toast.makeText(ctx, ctx.getString(R.string.update_ok), Toast.LENGTH_SHORT).show()
    }

    /**
     * 삭제
     */
    private fun setDeleteAppList() {
        val whereClause = "pkgnme=?"
        val whereArgs: Array<String>
        val pkg_name: String?

        pkg_name = getItem(clickedRow).getAPP_PKG_NAME().trim()
        getItem(clickedRow).setAPP_GBN("0")
        getItem(clickedRow).setAPP_OPTION1(option1.orEmpty())
        getItem(clickedRow).setAPP_OPTION2("N")

        whereArgs = arrayOf(pkg_name)

        try {
            for (i in 0 until infoList!!.size) {
                if (infoList!![i].getAPP_PKG_NAME().trim().equals(pkg_name, ignoreCase = true)) {
                    infoList!!.removeAt(i)
                    break
                }
            }

            dbm.executeDelete("applist", whereClause, whereArgs)

            notifyDataSetChanged()
        } catch (e: Exception) {
            Toast.makeText(ctx, ctx.getString(R.string.delete_err), Toast.LENGTH_SHORT).show()
            common.setLogMsg("삭제 오류 :: $e")
            return
        }
        Toast.makeText(ctx, ctx.getString(R.string.delete_ok), Toast.LENGTH_SHORT).show()
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