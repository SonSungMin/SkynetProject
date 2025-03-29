package com.skynet.skytimelock.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.content.DialogInterface.OnCancelListener
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.skytimelock.free.R

// 커스텀 리스너 인터페이스 정의
interface OnSettingDialogListener {
    fun onSave(dialog: SkySettingDialog)
}

@SuppressLint("Instantiatable")
class SkySettingDialog(
    context: Context,
    private val PKGIMG: Drawable,
    private val PKGNAME: String,
    private val PKGID: String,
    private val LMTTIME: String,
    private val SELECTED_INDEX: String,
    private val OPTION1: String?,
    private val OPTION2: String,
    private val ISCHECKEDAPP: Boolean,
    private val SELECTED_CNT: Int
) : Dialog(context) {
    private lateinit var edt_set_time: EditText
    private var _settingDialogListener: OnSettingDialogListener? = null
    private lateinit var rdo_set1: RadioButton
    private lateinit var rdo_set2: RadioButton
    private lateinit var rdo_set3: RadioButton
    private var isCancel = false

    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting

    private lateinit var dialog_icon: ImageView
    private lateinit var img_set_broken: ImageView
    private lateinit var img_set_number: ImageView
    private lateinit var img_set_pattern: ImageView
    private lateinit var img_set_wifi: ImageView

    private lateinit var lay_time_set: LinearLayout

    private lateinit var txt_dialog_title: TextView

    /**
     * 1:고장화면, 2:패스워드, 3:패턴
     */
    private var option1: String = ""
    private var option2: String = ""

    private val ctx: Context = context

    // 리스너 설정 메서드 추가
    fun setOnSettingDialogListener(listener: OnSettingDialogListener) {
        _settingDialogListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lpWindow = WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 1.0f
        }
        window?.attributes = lpWindow

        common = SkyTimeLockCommon(context)
        setting = common.getSetting("SkySettingDialog")

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_app_set)

        txt_dialog_title = findViewById(R.id.txt_dialog_title)

        dialog_icon = findViewById(R.id.dialog_icon)

        if (OPTION1 != null) {
            option1 = OPTION1
        }
        option2 = if (OPTION2 == null || "null" == OPTION2) "N" else OPTION2

        img_set_broken = findViewById(R.id.img_set_broken)
        img_set_number = findViewById(R.id.img_set_number)
        img_set_pattern = findViewById(R.id.img_set_pattern)
        img_set_wifi = findViewById(R.id.img_set_wifi)

        img_set_broken.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_broken_dis))
        img_set_number.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_number_dis))
        img_set_pattern.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_pattern_dis))

        when (option1) {
            "1" -> img_set_broken.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_broken))
            "2" -> img_set_number.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_number))
            "3" -> img_set_pattern.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_pattern))
        }

        if (option2 == "Y") {
            img_set_wifi.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_wifi))
        } else {
            img_set_wifi.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_wifi_dis))
        }

        img_set_broken.setOnClickListener(onClickListener)
        img_set_number.setOnClickListener(onClickListener)
        img_set_pattern.setOnClickListener(onClickListener)
        img_set_wifi.setOnClickListener(onClickListener)

        // 일괄 설정 앱
        if (ISCHECKEDAPP) {
            txt_dialog_title.text = context.getString(R.string.txt_batch) + " ($SELECTED_CNT)"
            dialog_icon.setImageDrawable(ctx.resources.getDrawable(R.drawable.main_icon))
        } else {
            txt_dialog_title.text = "$PKGNAME ${context.getString(R.string.txt_setting)}"
            dialog_icon.setImageDrawable(PKGIMG)
        }

        edt_set_time = findViewById(R.id.edt_set_time)
        val defaultTime = if (LMTTIME == null || "0" == LMTTIME || "" == LMTTIME) (10 * 1000 * 60).toString() else LMTTIME
        edt_set_time.setText((defaultTime.toLong() / 1000 / 60).toString())

        rdo_set1 = findViewById(R.id.rdo_set1)
        rdo_set1.setOnClickListener(onClickListener)
        rdo_set2 = findViewById(R.id.rdo_set2)
        rdo_set2.setOnClickListener(onClickListener)
        rdo_set3 = findViewById(R.id.rdo_set3)
        rdo_set3.setOnClickListener(onClickListener)

        val btn_set_plus = findViewById<Button>(R.id.btn_set_plus)
        btn_set_plus.setOnClickListener(onClickListener)
        val btn_set_minus = findViewById<Button>(R.id.btn_set_minus)
        btn_set_minus.setOnClickListener(onClickListener)

        val btn_set_save = findViewById<TextView>(R.id.txt_save)
        btn_set_save.setOnClickListener(onClickListener)
        val btn_set_cancel = findViewById<TextView>(R.id.txt_cancle)
        btn_set_cancel.setOnClickListener(onClickListener)

        lay_time_set = findViewById(R.id.lay_time_set)

        initRadio()
    }

    private fun initRadio() {
        // 시간 제한
        if (SELECTED_INDEX == "3") {
            rdo_set1.isChecked = true
            rdo_set1.visibility = View.VISIBLE
            lay_time_set.visibility = View.VISIBLE

            rdo_set2.isChecked = false
            rdo_set2.visibility = View.GONE
            rdo_set3.isChecked = false
        }
        // 잠금
        else if (SELECTED_INDEX == "4") {
            rdo_set1.isChecked = false
            rdo_set1.visibility = View.GONE
            lay_time_set.visibility = View.GONE

            rdo_set2.isChecked = true
            rdo_set2.visibility = View.VISIBLE
            rdo_set3.isChecked = false
        }

        setRadioText()
    }

    private fun setRadioText() {
        rdo_set1.textSize = 14f
        rdo_set2.textSize = 14f
        rdo_set3.textSize = 14f

        rdo_set1.setShadowLayer(0f, 0f, 0f, Color.BLACK)
        rdo_set2.setShadowLayer(0f, 0f, 0f, Color.BLACK)
        rdo_set3.setShadowLayer(0f, 0f, 0f, Color.BLACK)

        when {
            rdo_set1.isChecked -> rdo_set1.textSize = 14f
            rdo_set2.isChecked -> rdo_set2.textSize = 14f
            rdo_set3.isChecked -> rdo_set3.textSize = 14f
        }
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.txt_save -> setSave()

            R.id.txt_cancle -> {
                isCancel = true
                cancel()
            }

            R.id.btn_set_plus -> {
                val sTime = if (edt_set_time.text.toString() == "") "0" else edt_set_time.text.toString()
                val time = sTime.toInt()
                edt_set_time.setText((time + 10).toString())
            }

            R.id.btn_set_minus -> {
                val sTime = if (edt_set_time.text.toString() == "") "0" else edt_set_time.text.toString()
                var time = sTime.toInt()
                time -= 10
                if (time < 0) time = 0
                edt_set_time.setText(time.toString())
            }

            R.id.rdo_set1 -> {
                rdo_set1.isChecked = true
                rdo_set2.isChecked = false
                rdo_set3.isChecked = false
                setRadioText()
            }

            R.id.rdo_set2 -> {
                rdo_set1.isChecked = false
                rdo_set2.isChecked = true
                rdo_set3.isChecked = false
                setRadioText()
            }

            R.id.rdo_set3 -> {
                rdo_set1.isChecked = false
                rdo_set2.isChecked = false
                rdo_set3.isChecked = true
                setRadioText()

                setSave()
            }

            R.id.img_set_broken -> {
                img_set_broken.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_broken))
                img_set_number.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_number_dis))
                img_set_pattern.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_pattern_dis))
                option1 = "1"
                Toast.makeText(ctx, ctx.getString(R.string.rdo_method1), Toast.LENGTH_SHORT).show()
            }

            R.id.img_set_number -> {
                img_set_broken.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_broken_dis))
                img_set_number.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_number))
                img_set_pattern.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_pattern_dis))
                option1 = "2"
                Toast.makeText(ctx, ctx.getString(R.string.rdo_method2), Toast.LENGTH_SHORT).show()
            }

            R.id.img_set_pattern -> {
                img_set_broken.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_broken_dis))
                img_set_number.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_number_dis))
                img_set_pattern.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_pattern))

                if (setting.getPattern() == null || "" == setting.getPattern()) {
                    val i = Intent(ctx, SkyLockPatternView::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(SkyLockPatternView._Mode, SkyLockPatternView.LPMode.CreatePattern)
                        putExtra("MODE", "PWD") // 실행 버전 : ACT, 암호설정 버전 : PWD
                    }
                    ctx.startActivity(i)
                }

                option1 = "3"
                Toast.makeText(ctx, ctx.getString(R.string.rdo_method3), Toast.LENGTH_SHORT).show()
            }

            R.id.img_set_wifi -> {
                if (option2 == "Y") {
                    img_set_wifi.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_wifi_dis))
                    option2 = "N"
                } else {
                    img_set_wifi.setImageDrawable(ctx.resources.getDrawable(R.drawable.icon_wifi))
                    option2 = "Y"
                    Toast.makeText(ctx, ctx.getString(R.string.dialog_option_wifi), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setSave() {
        // 수정된 부분: 저장 시 커스텀 리스너 호출
        _settingDialogListener?.onSave(this)

        common.setRefreshSetting(true)

        isCancel = false
        cancel()
    }

    // Dialog 클래스의 메서드 오버라이드 - 부모 메서드 호출 추가
    override fun setOnCancelListener(listener: OnCancelListener?) {
        super.setOnCancelListener(listener)
    }

    /**
     * 선택한 방법 (0:Cancel, 1:시간제한, 2:앱잠금)
     */
    fun getType(): Int {
        return when {
            isCancel -> 0
            rdo_set1.isChecked -> 1
            rdo_set2.isChecked -> 2
            else -> 3
        }
    }

    /**
     * back 버튼 눌렸을때
     */
    override fun onBackPressed() {
        isCancel = true
        cancel()
    }

    /**
     * 설전된 시간(분)
     */
    fun getTime(): Int {
        return edt_set_time.text.toString().toInt()
    }

    /**
     * 잠금화면 (1:고장화면, 2:패스워드, 3:패턴)
     */
    fun getOption1(): String {
        return option1
    }

    /**
     * wifi 잠금 (Y, N)
     */
    fun getOption2(): String {
        return option2
    }
}