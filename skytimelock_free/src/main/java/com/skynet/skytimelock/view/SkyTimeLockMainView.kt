package com.skynet.skytimelock.view

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.free.R
import com.skynet.skytimelock.free.SkyDiviceAdmin
import com.skynet.skytimelock.free.SkyPersistentService

class SkyTimeLockMainView : SkyNetBaseActivity() {
    // 기기관리자
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private val REQUEST_CODE_ENABLE_ADMIN = 1

    private var SEC = 0

    /**
     * 잠금화면
     */
    private lateinit var rdo_group_method: RadioGroup
    private lateinit var rdo_method1: RadioButton
    private lateinit var rdo_method2: RadioButton
    private lateinit var rdo_method3: RadioButton

    /**
     * 잠금 방법
     */
    private lateinit var rdo_group_lock_method: RadioGroup
    private lateinit var rdo_lock_method1: RadioButton
    private lateinit var rdo_lock_method2: RadioButton

    private lateinit var txt_method_desc: TextView
    private lateinit var txt_lock_method_desc: TextView
    private lateinit var chk_option6_desc: TextView
    private lateinit var txt_common_desc: TextView

    private lateinit var txt_hr: TextView
    private lateinit var txt_min: TextView
    private lateinit var txt_sec: TextView

    private lateinit var txt_rank1: TextView
    private lateinit var txt_rank2: TextView
    private lateinit var txt_rank3: TextView

    private lateinit var txt_usedtime1: TextView
    private lateinit var txt_usedtime2: TextView
    private lateinit var txt_usedtime3: TextView

    private lateinit var txt_rate1: TextView
    private lateinit var txt_rate2: TextView
    private lateinit var txt_rate3: TextView

    private lateinit var lay_prog1: LinearLayout
    private lateinit var lay_prog2: LinearLayout
    private lateinit var lay_prog3: LinearLayout

    private lateinit var lay_rank1: LinearLayout
    private lateinit var lay_rank2: LinearLayout
    private lateinit var lay_rank3: LinearLayout

    private lateinit var lay_activity: LinearLayout
    private lateinit var lay_allow_app: LinearLayout
    private lateinit var activity_desc: LinearLayout
    private lateinit var layout_period: LinearLayout
    private lateinit var lay_time_activity: LinearLayout
    private lateinit var lay_time_desc: LinearLayout

    /**
     * 남은 시간 표시
     */
    private lateinit var chk_option1: CheckBox
    /**
     * 설정화면 잠금
     */
    private lateinit var chk_option2: CheckBox
    /**
     * 무작위 배경
     */
    private lateinit var chk_option3: CheckBox
    /**
     * 잠금활성화
     */
    private lateinit var chk_option4: CheckBox
    /**
     * 어플 삭제 보호
     */
    private lateinit var chk_option5: CheckBox
    /**
     * 블루라이트 필터
     */
    private lateinit var chk_option6: CheckBox
    /**
     * 자동시작
     */
    private lateinit var chk_option7: CheckBox
    /**
     * 신규 설치시 앱 잠금
     */
    private lateinit var chk_option9: CheckBox
    /**
     * 잠금 해제 활성화
     */
    private lateinit var chk_option10: CheckBox

    private var isTwo = false
    private var isClicked = false
    private var isInstalledDemon = false

    private val ACTION = "com.skynet.skytimelock.main.TIMEGUIDE_SERVICE"
    private val ACTION_DEMON = "action.service.SkyTimeLockHelper"
    private val DEFAULT_TAB_COLOR = "#F5D08A"
    private val SELECTED_TAB_COLOR = "#e5a25a"

    // 추천 필수 잠금 어플
    private val REQUIRD_APP = arrayOf("com.android.vending", "com.android.settings")
    // 추천 항상 허용 어플
    private val ALLOWED_APP = arrayOf("com.android.contacts", "com.android.mms")

    private val PHOTO_FILE_PATH = "${Environment.getExternalStorageDirectory()}/skynet/images"
    private var galleryFilePath: Array<String>? = null

    private lateinit var dbm: DbManager
    private lateinit var setting: SkyTimeLockSetting

    private lateinit var btn_delete: Button

    private lateinit var btn_back_img: Button
    private lateinit var btn_setting: Button
    private lateinit var btn_logcat_send: Button
    private lateinit var btn_setting_status: Button
    private lateinit var btn_setting_remote: Button
    private lateinit var btn_setting_remote_unlock: Button
    private lateinit var btn_lock_setting: Button
    private lateinit var btn_daily_setting: Button
    /**
     * 제한 시간 설정 버튼
     */
    private lateinit var btn_prior_setting: Button
    /**
     * 데몬 설치 버튼
     */
    private lateinit var btn_install_demon: Button
    /**
     * 잠금 어플 등록 버튼
     */
    private lateinit var btn_lock_app_setting: Button
    private lateinit var btn_graph: Button

    private lateinit var btn_setting_bluelight: Button

    private lateinit var tbtn_mon: ToggleButton
    private lateinit var tbtn_tue: ToggleButton
    private lateinit var tbtn_wed: ToggleButton
    private lateinit var tbtn_thu: ToggleButton
    private lateinit var tbtn_fri: ToggleButton
    private lateinit var tbtn_sat: ToggleButton
    private lateinit var tbtn_sun: ToggleButton

    private val REQ_CODE_PICK_IMAGE = 0
    private val PHOTO_FILE = "skynet_timelock_bg_"  // 임시 저장파일

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(true)
        super.setUpdateCheck(true)
        super.setLayout_Id(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // DB 파일 복사
        dbm = DbManager(this.applicationContext)

        common = SkyTimeLockCommon(this.applicationContext)
        setting = common.getSetting("SkyTimeLockMainView.onCreate")

        chk_option1 = findViewById(R.id.chk_option1)
        chk_option2 = findViewById(R.id.chk_option2)
        chk_option3 = findViewById(R.id.chk_option3)
        chk_option4 = findViewById(R.id.chk_option4)
        chk_option5 = findViewById(R.id.chk_option5)
        chk_option6 = findViewById(R.id.chk_option6)
        chk_option7 = findViewById(R.id.chk_option7)
        chk_option9 = findViewById(R.id.chk_option9)
        chk_option10 = findViewById(R.id.chk_option10)
        chk_option1.setOnClickListener(chkClick)
        chk_option2.setOnClickListener(chkClick)
        chk_option3.setOnClickListener(chkClick)
        chk_option4.setOnClickListener(chkClick)
        chk_option5.setOnClickListener(chkClick)
        chk_option6.setOnClickListener(chkClick)
        chk_option7.setOnClickListener(chkClick)
        chk_option9.setOnClickListener(chkClick)
        chk_option10.setOnClickListener(chkClick)

        txt_hr = findViewById(R.id.txt_hr)
        txt_min = findViewById(R.id.txt_min)
        txt_sec = findViewById(R.id.txt_sec)

        txt_rank1 = findViewById(R.id.txt_rank1)
        txt_rank2 = findViewById(R.id.txt_rank2)
        txt_rank3 = findViewById(R.id.txt_rank3)

        txt_usedtime1 = findViewById(R.id.txt_usedtime1)
        txt_usedtime2 = findViewById(R.id.txt_usedtime2)
        txt_usedtime3 = findViewById(R.id.txt_usedtime3)

        txt_rate1 = findViewById(R.id.txt_rate1)
        txt_rate2 = findViewById(R.id.txt_rate2)
        txt_rate3 = findViewById(R.id.txt_rate3)

        lay_rank1 = findViewById(R.id.lay_rank1)
        lay_rank2 = findViewById(R.id.lay_rank2)
        lay_rank3 = findViewById(R.id.lay_rank3)

        lay_prog1 = findViewById(R.id.lay_prog1)
        lay_prog2 = findViewById(R.id.lay_prog2)
        lay_prog3 = findViewById(R.id.lay_prog3)

        chk_option6_desc = findViewById(R.id.chk_option6_desc)
        txt_common_desc = findViewById(R.id.txt_common_desc)

        txt_method_desc = findViewById(R.id.txt_method_desc)
        txt_lock_method_desc = findViewById(R.id.txt_lock_method_desc)

        lay_activity = findViewById(R.id.lay_activity)
        lay_allow_app = findViewById(R.id.lay_allow_app)
        activity_desc = findViewById(R.id.activity_desc)
        layout_period = findViewById(R.id.layout_period)
        lay_time_activity = findViewById(R.id.lay_time_activity)
        lay_time_desc = findViewById(R.id.lay_time_desc)

        btn_setting_bluelight = findViewById(R.id.btn_setting_bluelight)
        btn_setting_bluelight.setOnClickListener(btn_clickEvent)

        btn_graph = findViewById(R.id.btn_graph)
        btn_graph.setOnClickListener(btn_clickEvent)

        btn_setting = findViewById(R.id.btn_setting)
        btn_setting.setOnClickListener { v ->
            setting = common.getSetting("SkyTimeLockMainView.btn_setting")

            when (setting.getMode()) {
                "1" -> {
                    val i = Intent(this@SkyTimeLockMainView, SkyDialogActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("PKGNAME", "")
                    }
                    this@SkyTimeLockMainView.startActivity(i)
                }
                "2" -> {
                    val i = Intent(this@SkyTimeLockMainView, SkyLockView::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("MODE", "PWD") // 실행 버전 : ACT, 암호설정 버전 : PWD
                        putExtra("ISLOCK", false)
                        putExtra("ISDELETED", false)
                        putExtra("LMTTIME", 0L)
                        putExtra("PKGNME", "-1")
                    }
                    this@SkyTimeLockMainView.startActivity(i)
                }
                "3" -> {
                    val i = Intent(this@SkyTimeLockMainView, SkyLockPatternView::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(SkyLockPatternView._Mode, SkyLockPatternView.LPMode.CreatePattern)
                        putExtra("MODE", "PWD") // 실행 버전 : ACT, 암호설정 버전 : PWD
                    }
                    this@SkyTimeLockMainView.startActivity(i)
                }
            }
        }

        btn_delete = findViewById(R.id.btn_delete)
        btn_delete.setOnClickListener(btn_clickEvent)

        btn_back_img = findViewById(R.id.btn_setting_bg)
        btn_back_img.setOnClickListener(btn_clickEvent)

        btn_lock_setting = findViewById(R.id.btn_lock_setting)
        btn_lock_setting.setOnClickListener(btn_clickEvent)

        btn_logcat_send = findViewById(R.id.btn_logcat_send)
        btn_logcat_send.setOnClickListener(btn_clickEvent)

        btn_setting_remote = findViewById(R.id.btn_setting_remote)
        btn_setting_remote.setOnClickListener(btn_clickEvent)

        btn_setting_remote_unlock = findViewById(R.id.btn_setting_remote_unlock)
        btn_setting_remote_unlock.setOnClickListener(btn_clickEvent)

        btn_setting_status = findViewById(R.id.btn_setting_status)
        btn_setting_status.setOnClickListener(btn_clickEvent)

        btn_prior_setting = findViewById(R.id.btn_prior_setting)
        btn_prior_setting.setOnClickListener(btn_clickEvent)

        btn_daily_setting = findViewById(R.id.btn_daily_setting)
        btn_daily_setting.setOnClickListener(btn_clickEvent)

        btn_install_demon = findViewById(R.id.btn_install_demon)
        btn_install_demon.setOnClickListener(btn_clickEvent)

        btn_lock_app_setting = findViewById(R.id.btn_lock_app_setting)
        btn_lock_app_setting.setOnClickListener(btn_clickEvent)

        rdo_method1 = findViewById(R.id.rdo_method1)
        rdo_method1.visibility = View.GONE
        rdo_method2 = findViewById(R.id.rdo_method2)
        rdo_method3 = findViewById(R.id.rdo_method3)

        rdo_group_method = findViewById(R.id.rdo_group_method)
        rdo_group_method.setOnCheckedChangeListener { group, id ->
            setting = common.getSetting("SkyTimeLockMainView.rdo_group_method")

            if (group == rdo_group_method) {
                when (id) {
                    R.id.rdo_method1 -> {
                        setting.setMode("1")
                        txt_method_desc.setText(R.string.method1)
                    }
                    R.id.rdo_method2 -> {
                        setting.setMode("2")
                        txt_method_desc.setText(R.string.method2)
                    }
                    R.id.rdo_method3 -> {
                        setting.setMode("3")

                        txt_method_desc.setText(R.string.method3)

                        if (setting.getPattern() == null || "" == setting.getPattern()) {
                            val i = Intent(this@SkyTimeLockMainView, SkyLockPatternView::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(SkyLockPatternView._Mode, SkyLockPatternView.LPMode.CreatePattern)
                                putExtra("MODE", "PWD") // 실행 버전 : ACT, 암호설정 버전 : PWD
                            }
                            this@SkyTimeLockMainView.startActivity(i)
                        }
                    }
                }

                saveSetting()
            }
        }

        rdo_lock_method1 = findViewById(R.id.rdo_lock_method1)
        rdo_lock_method2 = findViewById(R.id.rdo_lock_method2)
        rdo_group_lock_method = findViewById(R.id.rdo_group_lock_method)
        rdo_group_lock_method.setOnCheckedChangeListener { group, id ->
            setting = common.getSetting("SkyTimeLockMainView.rdo_group_lock_method")

            when (id) {
                // 앱잠금
                R.id.rdo_lock_method1 -> {
                    setting.setLock_mode("1")
                    btn_lock_setting.visibility = View.GONE
                    lay_activity.visibility = View.GONE
                    lay_allow_app.visibility = View.GONE
                    activity_desc.visibility = View.GONE
                    layout_period.visibility = View.VISIBLE
                    lay_time_activity.visibility = View.VISIBLE
                    lay_time_desc.visibility = View.VISIBLE
                    txt_lock_method_desc.text = getText(R.string.txt_method1_desc)
                    txt_common_desc.text = getText(R.string.rdo_lock_method1_desc)

                    rdo_lock_method1.setTextColor(Color.BLUE)
                    rdo_lock_method2.setTextColor(Color.BLACK)
                    rdo_lock_method1.paintFlags = rdo_lock_method1.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG
                    rdo_lock_method2.paintFlags = rdo_lock_method2.paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv()
                }
                // 일일 사용시간 제한
                R.id.rdo_lock_method2 -> {
                    setting.setLock_mode("2")
                    btn_lock_setting.visibility = View.VISIBLE
                    lay_activity.visibility = View.VISIBLE
                    lay_allow_app.visibility = View.VISIBLE
                    activity_desc.visibility = View.VISIBLE
                    layout_period.visibility = View.GONE
                    lay_time_activity.visibility = View.GONE
                    lay_time_desc.visibility = View.GONE
                    txt_lock_method_desc.text = getText(R.string.txt_method2_desc)
                    txt_common_desc.text = getText(R.string.rdo_lock_method2_desc)

                    rdo_lock_method1.setTextColor(Color.BLACK)
                    rdo_lock_method2.setTextColor(Color.BLUE)
                    rdo_lock_method1.paintFlags = rdo_lock_method1.paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv()
                    rdo_lock_method2.paintFlags = rdo_lock_method2.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG
                }
            }
            saveSetting()
        }

        tbtn_mon = findViewById(R.id.togbtn_mon)
        tbtn_mon.setOnClickListener(TogOnClickListener)
        tbtn_tue = findViewById(R.id.togbtn_tue)
        tbtn_tue.setOnClickListener(TogOnClickListener)
        tbtn_wed = findViewById(R.id.togbtn_wed)
        tbtn_wed.setOnClickListener(TogOnClickListener)
        tbtn_thu = findViewById(R.id.togbtn_thu)
        tbtn_thu.setOnClickListener(TogOnClickListener)
        tbtn_fri = findViewById(R.id.togbtn_fri)
        tbtn_fri.setOnClickListener(TogOnClickListener)
        tbtn_sat = findViewById(R.id.togbtn_sat)
        tbtn_sat.setOnClickListener(TogOnClickListener)
        tbtn_sun = findViewById(R.id.togbtn_sun)
        tbtn_sun.setOnClickListener(TogOnClickListener)

        loadSetting()
        checkInstallDemon()

        // 사용 시간 조회
        getUsedTime()

        MarketUpdate()
    }

    /**
     * 실행 주기 요일 버튼
     */
    private val TogOnClickListener = OnClickListener {
        setPrior()
    }

    private fun setPrior() {
        val prior = StringBuilder()

        if (tbtn_sun.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_mon.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_tue.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_wed.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_thu.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_fri.isChecked) prior.append("1|") else prior.append("0|")
        if (tbtn_sat.isChecked) prior.append("1|") else prior.append("0|")

        common.setLogMsg("set :: ${prior.toString()}")
        setting.setPrior_value(prior.toString())

        saveSetting()
    }

    private fun getPrior() {
        val prior = setting.getPrior_value().split("|")

        tbtn_sun.isChecked = "1" == prior.getOrNull(1)
        tbtn_mon.isChecked = "1" == prior.getOrNull(3)
        tbtn_tue.isChecked = "1" == prior.getOrNull(5)
        tbtn_wed.isChecked = "1" == prior.getOrNull(7)
        tbtn_thu.isChecked = "1" == prior.getOrNull(9)
        tbtn_fri.isChecked = "1" == prior.getOrNull(11)
        tbtn_sat.isChecked = "1" == prior.getOrNull(13)
    }

    /**
     * 기기관리자 등록
     */
    private fun setActiviteDiviceManager() {
        // 기기관리자
        adminComponent = ComponentName(this, SkyDiviceAdmin::class.java)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why this needs to be added.")
            }
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        }
    }

    /**
     * 기기관리자 해제
     */
    private fun setDeActiviteDiviceManager() {
        // 기기관리자
        adminComponent = ComponentName(this, SkyDiviceAdmin::class.java)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.removeActiveAdmin(adminComponent)
            Toast.makeText(this, getString(R.string.no_admin), Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this@SkyTimeLockMainView, getString(R.string.ok_admin), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SkyTimeLockMainView, getString(R.string.no_admin), Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private var selectedIcon = "1"

    private lateinit var rdo_group_status: RadioGroup
    private lateinit var rdo_status1: RadioButton
    private lateinit var rdo_status2: RadioButton
    private lateinit var rdo_status3: RadioButton
    private lateinit var rdo_status4: RadioButton
    private lateinit var rdo_status5: RadioButton
    private lateinit var rdo_status6: RadioButton
    private lateinit var rdo_status7: RadioButton
    private lateinit var rdo_status8: RadioButton
    private lateinit var rdo_status9: RadioButton
    private lateinit var rdo_status10: RadioButton
    private lateinit var rdo_status11: RadioButton
    private lateinit var rdo_status12: RadioButton
    private lateinit var rdo_status13: RadioButton

    @SuppressLint("NewApi", "ShowToast")
    private fun createDialog(id: Int): AlertDialog? {
        var rtn: AlertDialog? = null

        when (id) {
            R.id.btn_setting_remote -> {
                val layout = layoutInflater.inflate(R.layout.sms_setting, null)

                val msg = layout.findViewById<EditText>(R.id.edt_sms_msg)
                msg.setText(common.getSMSLock())

                val builder = try {
                    AlertDialog.Builder(this@SkyTimeLockMainView, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                } catch (e: Exception) {
                    AlertDialog.Builder(this@SkyTimeLockMainView)
                }
                builder.setTitle(getString(R.string.txt_remote_setting_setting))
                builder.setView(layout)
                // 저장
                builder.setPositiveButton(getString(R.string.txt_apply)) { _, _ ->
                    common.setSMSLock(msg.text.toString())
                    Toast.makeText(this@SkyTimeLockMainView, getString(R.string.save_ok), Toast.LENGTH_SHORT).show()
                    common.setRefreshSetting(true)
                }
                // 취소
                builder.setNegativeButton(getString(R.string.txt_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                rtn = builder.create()
            }

            R.id.btn_setting_remote_unlock -> {
                val layout = layoutInflater.inflate(R.layout.sms_unlock_setting, null)

                val msg_unlock = layout.findViewById<EditText>(R.id.edt_sms_msg_unlock)
                msg_unlock.setText(common.getSMSUnLockMsg())

                val builder = try {
                    AlertDialog.Builder(this@SkyTimeLockMainView, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                } catch (e: Exception) {
                    AlertDialog.Builder(this@SkyTimeLockMainView)
                }
                builder.setTitle(getString(R.string.txt_remote_unlock_setting))
                builder.setView(layout)
                // 저장
                builder.setPositiveButton(getString(R.string.txt_apply)) { _, _ ->
                    common.setSMSUnLockMsg(msg_unlock.text.toString())
                    Toast.makeText(this@SkyTimeLockMainView, getString(R.string.save_ok), Toast.LENGTH_SHORT).show()
                    common.setRefreshSetting(true)
                }
                // 취소
                builder.setNegativeButton(getString(R.string.txt_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                rtn = builder.create()
            }

            R.id.btn_setting_status -> {
                val layout = layoutInflater.inflate(R.layout.dialog_status_img, null)

                rdo_status1 = layout.findViewById(R.id.rdo_status1)
                rdo_status1.setOnClickListener(radio_clickEvent)
                rdo_status2 = layout.findViewById(R.id.rdo_status2)
                rdo_status2.setOnClickListener(radio_clickEvent)
                rdo_status3 = layout.findViewById(R.id.rdo_status3)
                rdo_status3.setOnClickListener(radio_clickEvent)
                rdo_status4 = layout.findViewById(R.id.rdo_status4)
                rdo_status4.setOnClickListener(radio_clickEvent)
                rdo_status5 = layout.findViewById(R.id.rdo_status5)
                rdo_status5.setOnClickListener(radio_clickEvent)
                rdo_status6 = layout.findViewById(R.id.rdo_status6)
                rdo_status6.setOnClickListener(radio_clickEvent)
                rdo_status7 = layout.findViewById(R.id.rdo_status7)
                rdo_status7.setOnClickListener(radio_clickEvent)
                rdo_status8 = layout.findViewById(R.id.rdo_status8)
                rdo_status8.setOnClickListener(radio_clickEvent)
                rdo_status9 = layout.findViewById(R.id.rdo_status9)
                rdo_status9.setOnClickListener(radio_clickEvent)
                rdo_status10 = layout.findViewById(R.id.rdo_status10)
                rdo_status10.setOnClickListener(radio_clickEvent)
                rdo_status11 = layout.findViewById(R.id.rdo_status11)
                rdo_status11.setOnClickListener(radio_clickEvent)
                rdo_status12 = layout.findViewById(R.id.rdo_status12)
                rdo_status12.setOnClickListener(radio_clickEvent)
                rdo_status13 = layout.findViewById(R.id.rdo_status13)
                rdo_status13.setOnClickListener(radio_clickEvent)

                val builder = try {
                    AlertDialog.Builder(this@SkyTimeLockMainView, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                } catch (e: Exception) {
                    AlertDialog.Builder(this@SkyTimeLockMainView)
                }
                builder.setTitle(getString(R.string.txt_status_setting))
                builder.setView(layout)
                // 저장
                builder.setPositiveButton(getString(R.string.txt_apply)) { _, _ ->
                    common.setStatusIcon(selectedIcon)
                    common.setChangeStatusIcon(true)
                    common.StartService()
                }
                // 취소
                builder.setNegativeButton(getString(R.string.txt_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }

                rtn = builder.create()
            }
        }

        return rtn
    }

    private val radio_clickEvent = OnClickListener { v ->
        when (v.id) {
            R.id.rdo_status1 -> {
                selectedIcon = "1"
                rdo_status1.isChecked = true; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status2 -> {
                selectedIcon = "2"
                rdo_status1.isChecked = false; rdo_status2.isChecked = true; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status3 -> {
                selectedIcon = "3"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = true
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status4 -> {
                selectedIcon = "4"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = true; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status5 -> {
                selectedIcon = "5"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = true; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status6 -> {
                selectedIcon = "6"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = true
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status7 -> {
                selectedIcon = "7"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = true; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status8 -> {
                selectedIcon = "8"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = true; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status9 -> {
                selectedIcon = "9"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = true
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status10 -> {
                selectedIcon = "10"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = true; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status11 -> {
                selectedIcon = "11"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = true; rdo_status12.isChecked = false
                rdo_status13.isChecked = false
            }

            R.id.rdo_status12 -> {
                selectedIcon = "12"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = true
                rdo_status13.isChecked = false
            }

            R.id.rdo_status13 -> {
                selectedIcon = "13"
                rdo_status1.isChecked = false; rdo_status2.isChecked = false; rdo_status3.isChecked = false
                rdo_status4.isChecked = false; rdo_status5.isChecked = false; rdo_status6.isChecked = false
                rdo_status7.isChecked = false; rdo_status8.isChecked = false; rdo_status9.isChecked = false
                rdo_status10.isChecked = false; rdo_status11.isChecked = false; rdo_status12.isChecked = false
                rdo_status13.isChecked = true
            }
        }
    }

    private val btn_clickEvent = OnClickListener { v ->
        when (v.id) {
            R.id.btn_delete -> {
                dbm.executeDelete("appcount", null, null)
                Toast.makeText(this@SkyTimeLockMainView, "Deleted !", Toast.LENGTH_SHORT).show()
            }

            R.id.btn_logcat_send -> {
                MakeLogCat()
            }

            R.id.btn_lock_setting -> {
                val able_list = Intent(this@SkyTimeLockMainView.applicationContext, SkyAbleList::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(able_list)
            }

            R.id.btn_setting_bluelight -> {
                val bluelight = Intent(this@SkyTimeLockMainView.applicationContext, SkyBlueLightView::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(bluelight)
            }

            R.id.btn_setting_bg -> {
                val i = Intent(this@SkyTimeLockMainView.applicationContext, SkyLockImageListView::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(i)
            }

            // SMS 잠금 설정
            R.id.btn_setting_remote -> {
                createDialog(R.id.btn_setting_remote)?.show()
            }

            R.id.btn_setting_remote_unlock -> {
                createDialog(R.id.btn_setting_remote_unlock)?.show()
            }

            // 상태바 아이콘
            R.id.btn_setting_status -> {
                createDialog(R.id.btn_setting_status)?.show()
            }

            // 제한시간 설정 버튼
            R.id.btn_prior_setting -> {
                val prior_list = Intent(this@SkyTimeLockMainView.applicationContext, SkyPriorList::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(prior_list)
            }

            // 시간제한 앱 설정 버튼
            R.id.btn_daily_setting -> {
                val daily_list = Intent(this@SkyTimeLockMainView.applicationContext, SkyAppList::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("SELECTED_INDEX", 3)
                }
                startActivity(daily_list)
            }

            // 잠금 어플 등록
            R.id.btn_lock_app_setting -> {
                val lock_list = Intent(this@SkyTimeLockMainView.applicationContext, SkyAppList::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("SELECTED_INDEX", 4)
                }
                startActivity(lock_list)
            }

            R.id.btn_install_demon -> {
                installProcess()
            }

            R.id.btn_graph -> {
                val prog_list = Intent(this@SkyTimeLockMainView.applicationContext, SkyProgList::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(prog_list)
            }
        }
    }

    /**
     * 옵션 체크박스 클릭 이벤트
     */
    private val chkClick = OnClickListener { v ->
        setting = common.getSetting("SkyTimeLockMainView.chkClick")

        // 삭제 방지
        if (v.id == R.id.chk_option5) {
            if (chk_option5.isChecked) {
                setActiviteDiviceManager()
            } else {
                setDeActiviteDiviceManager()
            }
        }

        setting.setOption1(chk_option1.isChecked)
        setting.setOption2(chk_option2.isChecked)
        setting.setOption3(chk_option3.isChecked)
        setting.setOption4(chk_option4.isChecked)
        setting.setOption5(chk_option5.isChecked)
        setting.setOption6(chk_option6.isChecked)
        setting.setOption7(chk_option7.isChecked)
        setting.setOption9(chk_option9.isChecked)
        setting.setOption10(chk_option10.isChecked)
        saveSetting()
    }

    /**
     * 환경 설정 불러오기
     */
    private fun loadSetting() {
        // 환경설정 불러오기 ##############################
        setting = common.getSetting("SkyTimeLockMainView.loadSetting")
        // ##############################################

        // 고장화면
        when (setting.getMode()) {
            "1" -> {
                rdo_method1.isChecked = true
                rdo_method2.isChecked = false
                rdo_method3.isChecked = false
                txt_method_desc.setText(R.string.method1)
            }
            // 비번
            "2" -> {
                rdo_method1.isChecked = false
                rdo_method2.isChecked = true
                rdo_method3.isChecked = false
                txt_method_desc.setText(R.string.method2)
            }
            // 패턴
            "3" -> {
                rdo_method1.isChecked = false
                rdo_method2.isChecked = false
                rdo_method3.isChecked = true
                txt_method_desc.setText(R.string.method3)
            }
        }

        // 앱별 시간 제한
        when (setting.getLock_mode()) {
            "1" -> {
                rdo_lock_method1.isChecked = true
                rdo_lock_method2.isChecked = false
                btn_lock_setting.visibility = View.GONE
                lay_activity.visibility = View.GONE
                lay_allow_app.visibility = View.GONE
                activity_desc.visibility = View.GONE
                layout_period.visibility = View.VISIBLE
                txt_lock_method_desc.text = getText(R.string.txt_method1_desc)
                txt_common_desc.text = getText(R.string.rdo_lock_method1_desc)

                rdo_lock_method1.setTextColor(Color.BLUE)
                rdo_lock_method2.setTextColor(Color.BLACK)
                rdo_lock_method1.paintFlags = rdo_lock_method1.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG
                rdo_lock_method2.paintFlags = rdo_lock_method2.paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv()
            }
            // 일일 시간제한
            "2" -> {
                rdo_lock_method1.isChecked = false
                rdo_lock_method2.isChecked = true
                btn_lock_setting.visibility = View.VISIBLE
                lay_activity.visibility = View.VISIBLE
                lay_allow_app.visibility = View.VISIBLE
                activity_desc.visibility = View.VISIBLE
                layout_period.visibility = View.GONE
                txt_lock_method_desc.text = getText(R.string.txt_method2_desc)
                txt_common_desc.text = getText(R.string.rdo_lock_method2_desc)

                rdo_lock_method1.setTextColor(Color.BLACK)
                rdo_lock_method2.setTextColor(Color.BLUE)
                rdo_lock_method1.paintFlags = rdo_lock_method1.paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv()
                rdo_lock_method2.paintFlags = rdo_lock_method2.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG
            }
        }

        // 잠금 주기
        getPrior()

        chk_option1.isChecked = setting.isOption1()
        chk_option2.isChecked = setting.isOption2()
        chk_option3.isChecked = setting.isOption3()
        chk_option4.isChecked = setting.isOption4()

        adminComponent = ComponentName(this, SkyDiviceAdmin::class.java)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (setting.isOption5() && !devicePolicyManager.isAdminActive(adminComponent)) {
            setting.setOption5(false)
            chk_option5.isChecked = false
        } else {
            chk_option5.isChecked = true
        }

        chk_option6.isChecked = setting.isOption6()
        chk_option7.isChecked = setting.isOption7()
        chk_option9.isChecked = setting.isOption9()
        chk_option10.isChecked = setting.isOption10()

        if (!chk_option4.isChecked) {
            stopService(Intent(this@SkyTimeLockMainView, SkyPersistentService::class.java))
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getUsedTime() {
        var total_time = 0.0
        var app_time = 0.0
        var pkgnme: String
        var rank1: Long
        var rank2: Long
        var rank3: Long

        val height = 25

        // 사용 시간 조회
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val dTime = formatter.format(Date())
        val sql = "select sum(exetime) as exetime from appcount where exedte = ? and pkgnme <> '1'"
        val selectionArgs = arrayOf(dTime)
        val select = dbm.executeSelect(sql, selectionArgs)

        if (select != null && select.isNotEmpty() && select[0]["exetime"] != null) {
            total_time = select[0]["exetime"].toString().toLong().toDouble()
        }

        txt_hr.text = getHH(total_time)
        txt_min.text = getMM(total_time)
        txt_sec.text = getSEC(total_time)

        val sql2 = "select pkgnme, exetime from (select pkgnme, sum(exetime) as exetime, count(pkgnme)cnt from appcount where exedte = ? and pkgnme <> '1' group by pkgnme) order by exetime desc"
        val select2 = dbm.executeSelect(sql2, selectionArgs)

        if (select2 == null || select2.isEmpty()) {
            lay_rank1.visibility = View.GONE
            lay_rank2.visibility = View.GONE
            lay_rank3.visibility = View.GONE
        } else {
            when (select2.size) {
                1 -> {
                    app_time = select2[0]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[0]["pkgnme"].toString())
                    txt_rank1.text = pkgnme
                    txt_usedtime1.text = getCountDown(app_time)
                    rank1 = Math.round(app_time / total_time * 100)
                    txt_rate1.text = "$rank1%"
                    lay_prog1.layoutParams = LinearLayout.LayoutParams(0, height, rank1)

                    lay_rank2.visibility = View.GONE
                    lay_rank3.visibility = View.GONE
                }
                2 -> {
                    app_time = select2[0]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[0]["pkgnme"].toString())
                    txt_rank1.text = pkgnme
                    txt_usedtime1.text = getCountDown(app_time)
                    rank1 = Math.round(app_time / total_time * 100)
                    txt_rate1.text = "$rank1%"
                    lay_prog1.layoutParams = LinearLayout.LayoutParams(0, height, rank1)

                    app_time = select2[1]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[1]["pkgnme"].toString())
                    txt_rank2.text = pkgnme
                    txt_usedtime2.text = getCountDown(app_time)
                    rank2 = Math.round(app_time / total_time * 100)
                    txt_rate2.text = "$rank2%"
                    lay_prog2.layoutParams = LinearLayout.LayoutParams(0, height, rank2)

                    lay_rank3.visibility = View.GONE
                }
                else -> {
                    app_time = select2[0]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[0]["pkgnme"].toString())
                    txt_rank1.text = pkgnme
                    txt_usedtime1.text = getCountDown(app_time)
                    rank1 = Math.round(app_time / total_time * 100)
                    txt_rate1.text = "$rank1%"
                    lay_prog1.layoutParams = LinearLayout.LayoutParams(0, height, rank1)

                    app_time = select2[1]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[1]["pkgnme"].toString())
                    txt_rank2.text = pkgnme
                    txt_usedtime2.text = getCountDown(app_time)
                    rank2 = Math.round(app_time / total_time * 100)
                    txt_rate2.text = "$rank2%"
                    lay_prog2.layoutParams = LinearLayout.LayoutParams(0, height, rank2)

                    app_time = select2[2]["exetime"].toString().toLong().toDouble()
                    pkgnme = getAppNme(select2[2]["pkgnme"].toString())
                    txt_rank3.text = pkgnme
                    txt_usedtime3.text = getCountDown(app_time)
                    rank3 = Math.round(app_time / total_time * 100)
                    txt_rate3.text = "$rank3%"
                    lay_prog3.layoutParams = LinearLayout.LayoutParams(0, height, rank3)
                }
            }
        }
    }

    private fun getAppNme(pkgnme: String): String {
        var name = ""

        try {
            name = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(pkgnme, PackageManager.GET_UNINSTALLED_PACKAGES)
            ).toString()
        } catch (e1: NameNotFoundException) {
            e1.printStackTrace()
        }
        return name
    }

    /**
     * 데몬 설치 여부
     */
    @Synchronized
    private fun checkInstallDemon() {
        isInstalledDemon = false

        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages) {
            if ("com.skynet.skytimelock.skytimelockhelper".equals(packageInfo.packageName, ignoreCase = true)) {
                isInstalledDemon = true
                break
            }
        }
    }

    /**
     * 데몬 삭제
     */
    private fun unInstallProcess() {
        val packageURI = Uri.parse("package:com.skynet.skytimelock.skytimelockhelper")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
        startActivity(uninstallIntent)

        isInstalledDemon = false
    }

    /**
     * 데몬 설치
     */
    private fun installProcess() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.skynet.skytimelock.skytimelockhelper")))
    }

    /**
     * 환경 설정 저장
     */
    private fun saveSetting() {
        common.setSetting(setting)

        // 환경 설정파일 갱신
        common.setRefreshSetting(true)
    }

    /**
     * 백버튼이 눌렸을때
     */
    override fun onBackPressed() {
        if (!isTwo) {
            Toast.makeText(this, getText(R.string.txt_exit), Toast.LENGTH_SHORT).show()
            val timer = myTimer(2000, 1) // 2초동안 수행
            timer.start()
        } else {
            startMainService()
        }
    }

    inner class myTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        init {
            isTwo = true
        }

        override fun onFinish() {
            isTwo = false
        }

        override fun onTick(millisUntilFinished: Long) {
            // Nothing to do
        }
    }

    override fun onDestroy() {
        common.setRefreshSetting(true)
        super.onDestroy()
    }

    /**
     * 메인 서비스를 시작하고 환경 설정을 저장한다.
     */
    private fun startMainService() {
        finish()
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
}