package com.skynet.skytimelock.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.free.R
import java.util.HashMap

class SkyLockView : SkyNetBaseActivity() {
    private lateinit var edt_pwd: TextView
    private lateinit var layout_main: LinearLayout
    private lateinit var lay_keypad: LinearLayout

    private lateinit var btn_pad0: Button
    private lateinit var btn_pad1: Button
    private lateinit var btn_pad2: Button
    private lateinit var btn_pad3: Button
    private lateinit var btn_pad4: Button
    private lateinit var btn_pad5: Button
    private lateinit var btn_pad6: Button
    private lateinit var btn_pad7: Button
    private lateinit var btn_pad8: Button
    private lateinit var btn_pad9: Button
    private lateinit var btn_pad_ok: Button
    private lateinit var btn_pad_del: Button

    private lateinit var tv: TextView

    private var isLockView = false
    private var isDeleted = false
    private var pkgnme: String? = null
    private var sPref_pwd: String? = null
    private val defaultPwd = "777" // 초기 암호
    private var orgPwd: String? = null
    private var chgPwd: String? = null
    private var LMTTIME: Long = 0

    /**
     * 처음 실행 : START, 실행 버전 : ACT, 암호설정 버전 : PWD
     */
    private var MODE: String? = null

    private lateinit var setting: SkyTimeLockSetting

    private var sPref_bg: String? = null
    private var sPref_option3: Boolean = false
    private lateinit var dbm: DbManager
    private var curCnt = 0
    private lateinit var time_app_icon: ImageView

    private var inputPwd = ""

    private lateinit var dw: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(true)
        super.setLayout_Id(R.layout.lock_view)
        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dbm = DbManager(this)
        setting = common.getSetting("SkyLockView")

        sPref_pwd = if (setting.getPwd() == null || "null".equals(setting.getPwd(), ignoreCase = true)) "777" else setting.getPwd()
        sPref_bg = setting.getBg()
        sPref_option3 = setting.isOption3()

        orgPwd = sPref_pwd

        common.setIsCheckedPwd(false)

        // 배경 설정
        layout_main = findViewById(R.id.layout_lock_view)
        lay_keypad = findViewById(R.id.lay_keypad)

        MODE = intent.getStringExtra("MODE")
        isLockView = intent.getBooleanExtra("ISLOCK", false)
        isDeleted = intent.getBooleanExtra("ISDELETED", false)
        pkgnme = intent.getStringExtra("PKGNME")
        LMTTIME = intent.getLongExtra("LMTTIME", 0)

        // 잠금 어플 아이콘
        time_app_icon = findViewById(R.id.time_app_icon)

        dw = resources.getDrawable(R.drawable.main_icon)

        tv = findViewById(R.id.txt_msg)
        tv.setTextColor(Color.BLUE)

        if ("ACT" == MODE) {
            if (setting.isOption10())
                lay_keypad.visibility = View.VISIBLE
            else
                lay_keypad.visibility = View.GONE

            if (isLockView)
                tv.setText(R.string.lock_message1)
            else
                tv.setText(R.string.lock_message2)

            try {
                dw = packageManager.getApplicationIcon(pkgnme!!)
            } catch (e: NameNotFoundException) {
                dw = resources.getDrawable(R.drawable.main_icon)
            }
        } else {
            var msg = getText(R.string.msg_cur_pwd).toString()
            msg += "\n" + getText(R.string.init_pwd)
            // 현재 비번 입력 메시지
            tv.text = msg
        }

        time_app_icon.setImageDrawable(dw)

        // 암호 입력창 설정
        edt_pwd = findViewById(R.id.txt_pwd)
        edt_pwd.text = ""
        edt_pwd.clearFocus()
        edt_pwd.setOnClickListener(clickListener)
        edt_pwd.isSingleLine = true
        edt_pwd.setBackgroundColor(Color.TRANSPARENT)
        edt_pwd.setTextColor(Color.WHITE)
        edt_pwd.inputType = 0
        edt_pwd.isFocusable = false

        btn_pad0 = findViewById(R.id.btn_pad0)
        btn_pad1 = findViewById(R.id.btn_pad1)
        btn_pad2 = findViewById(R.id.btn_pad2)
        btn_pad3 = findViewById(R.id.btn_pad3)
        btn_pad4 = findViewById(R.id.btn_pad4)
        btn_pad5 = findViewById(R.id.btn_pad5)
        btn_pad6 = findViewById(R.id.btn_pad6)
        btn_pad7 = findViewById(R.id.btn_pad7)
        btn_pad8 = findViewById(R.id.btn_pad8)
        btn_pad9 = findViewById(R.id.btn_pad9)
        btn_pad_ok = findViewById(R.id.btn_pad_ok)
        btn_pad_del = findViewById(R.id.btn_pad_del)

        btn_pad0.setOnClickListener(clickListener)
        btn_pad1.setOnClickListener(clickListener)
        btn_pad2.setOnClickListener(clickListener)
        btn_pad3.setOnClickListener(clickListener)
        btn_pad4.setOnClickListener(clickListener)
        btn_pad5.setOnClickListener(clickListener)
        btn_pad6.setOnClickListener(clickListener)
        btn_pad7.setOnClickListener(clickListener)
        btn_pad8.setOnClickListener(clickListener)
        btn_pad9.setOnClickListener(clickListener)
        btn_pad_ok.setOnClickListener(clickListener)
        btn_pad_del.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        if ("ACT" == MODE || isLockView) {
            val i = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(i)
        } else {
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onStart() {
        layout_main.background = common.getBackgroundImg()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    private val clickListener = OnClickListener { arg ->
        val txt = edt_pwd.text?.toString() ?: ""

        when (arg.id) {
            R.id.btn_pad0 -> inputPwd += "0"
            R.id.btn_pad1 -> inputPwd += "1"
            R.id.btn_pad2 -> inputPwd += "2"
            R.id.btn_pad3 -> inputPwd += "3"
            R.id.btn_pad4 -> inputPwd += "4"
            R.id.btn_pad5 -> inputPwd += "5"
            R.id.btn_pad6 -> inputPwd += "6"
            R.id.btn_pad7 -> inputPwd += "7"
            R.id.btn_pad8 -> inputPwd += "8"
            R.id.btn_pad9 -> inputPwd += "9"

            // 확인 버튼(OK)
            R.id.btn_pad_ok -> {
                // 실행 모드
                if ("ACT" == MODE) {
                    // 올바른 비번을 입력했을 경우
                    if (sPref_pwd == inputPwd) {
                        common.setIsCheckedPwd(true)
                        // sms 잠금 설정 비활성화
                        common.setAllLock(false)

                        if (!isLockView) {
                            if (setting.getLock_mode() == "1") {
                                val values = HashMap<Object, Object>()
                                values["exe_eddte"] = System.currentTimeMillis() as Object
                                values["spctime"] = LMTTIME as Object
                                val whereClause = "pkgnme=?"

                                try {
                                    dbm.executeUpdate("applist", values, whereClause, arrayOf(pkgnme!!))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        inputPwd = ""
                        edt_pwd.text = ""

                        tv.setTextColor(Color.RED)
                        tv.text = getText(R.string.msg_cur_pwd_err)
                        return@OnClickListener
                    }
                } else if ("START" == MODE) {
                    // 비번이 틀렸을 경우
                    if (sPref_pwd != inputPwd) {
                        tv.setTextColor(Color.RED)
                        tv.text = getText(R.string.msg_cur_pwd_err)
                        inputPwd = ""
                        edt_pwd.text = ""

                        return@OnClickListener
                    }
                    val intent = Intent(this@SkyLockView, SkyTimeLockMainView::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                } else {
                    // 비번 변경 모드
                    tv.setTextColor(Color.BLUE)

                    // 올바른 초기 비번을 입력했을 경우
                    if (curCnt == 0 && sPref_pwd == inputPwd) {
                        // 변경할 비번 입력
                        tv.text = getText(R.string.msg_cur_pwd_chg)

                        edt_pwd.text = ""
                        inputPwd = ""

                        curCnt++

                        return@OnClickListener
                    } else if (curCnt == 1) {
                        // 변경할 비번 입력
                        // 한 번 더 입력
                        chgPwd = inputPwd
                        tv.text = getText(R.string.msg_cur_pwd_chg_again)

                        edt_pwd.text = ""
                        inputPwd = ""
                        curCnt++

                        return@OnClickListener
                    } else if (curCnt == 2 && chgPwd == inputPwd) {
                        // 한 번 더 입력
                        chgPwd = inputPwd
                        tv.text = getText(R.string.msg_cur_pwd_chg_ok)

                        edt_pwd.text = ""

                        setting.setPwd(chgPwd!!)
                        common.setSetting(setting)

                        inputPwd = ""

                        Toast.makeText(this@SkyLockView, getText(R.string.msg_cur_pwd_chg_ok), Toast.LENGTH_SHORT).show()
                    } else {
                        tv.setTextColor(Color.RED)
                        tv.text = getText(R.string.msg_cur_pwd_err)
                        edt_pwd.text = ""
                        inputPwd = ""

                        if (curCnt > 0) {
                            curCnt = 1
                        }

                        return@OnClickListener
                    }
                }
                finish()
            }

            // 글자 삭제 버튼
            R.id.btn_pad_del -> {
                if (txt == "") {
                    edt_pwd.text = ""
                } else if (inputPwd.isNotEmpty()) {
                    inputPwd = inputPwd.substring(0, inputPwd.length - 1)
                    edt_pwd.text = lpad(inputPwd, "*")
                }
            }
        }

        checkPwd()
        edt_pwd.text = lpad(inputPwd, "*")
    }

    private fun checkPwd() {
        if (sPref_pwd == inputPwd) {
            if ("ACT" == MODE) {
                if (!isLockView) {
                    val values = HashMap<Object, Object>()
                    values["exe_eddte"] = System.currentTimeMillis() as Object
                    values["spctime"] = LMTTIME as Object
                    val whereClause = "pkgnme=?"

                    try {
                        dbm.executeUpdate("applist", values, whereClause, arrayOf(pkgnme!!))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                common.setIsCheckedPwd(true)
                // sms 잠금 비활성화
                common.setAllLock(false)

                finish()
            } else if ("START" == MODE) {
                val intent = Intent(this@SkyLockView, SkyTimeLockMainView::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)

                finish()
            }
        }
    }

    private fun lpad(str: String, addStr: String): String {
        return if (str.length > 1) {
            val result = StringBuilder()
            val templen = str.length - 1

            for (i in 0 until templen) {
                result.append(addStr)
            }

            result.toString() + str.substring(str.length - 1)
        } else {
            str
        }
    }
}