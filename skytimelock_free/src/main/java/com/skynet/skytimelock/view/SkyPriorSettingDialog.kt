package com.skynet.skytimelock.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.skytimelock.free.R

@SuppressLint("Instantiatable")
class SkyPriorSettingDialog(context: Context, dayweek: String, lmttime: Long, spctime: Long) : Dialog(context) {
    private var _cancellistener: OnCancelListener? = null

    private lateinit var togbtn_sun: ToggleButton
    private lateinit var togbtn_mon: ToggleButton
    private lateinit var togbtn_tue: ToggleButton
    private lateinit var togbtn_wed: ToggleButton
    private lateinit var togbtn_thu: ToggleButton
    private lateinit var togbtn_fri: ToggleButton
    private lateinit var togbtn_sat: ToggleButton

    private lateinit var btn_set_plus_lock: Button
    private lateinit var btn_set_minus_lock: Button

    private lateinit var txt_lock_time_hour: TextView
    private lateinit var txt_lock_time_min: TextView
    private lateinit var txt_lock_able_hour: TextView
    private lateinit var txt_lock_able_min: TextView

    var lmttime: Long = lmttime
    var spctime: Long = spctime
    var dayweek: String = "0|0|0|0|0|0|0|"
    private val param_dayweek: String = dayweek

    /**
     * 저장하지 않고 취소 버튼을 누른 경우 true
     */
    private var isCancel = false

    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting

    private val ctx: Context = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lpWindow = WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 1.0f
        }
        window?.attributes = lpWindow

        common = SkyTimeLockCommon(context)
        setting = common.getSetting("SkyPriorSettingDialog")

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_prior_set)

        togbtn_mon = findViewById(R.id.togbtn_mon)
        togbtn_mon.setOnClickListener(TogOnClickListener)
        togbtn_tue = findViewById(R.id.togbtn_tue)
        togbtn_tue.setOnClickListener(TogOnClickListener)
        togbtn_wed = findViewById(R.id.togbtn_wed)
        togbtn_wed.setOnClickListener(TogOnClickListener)
        togbtn_thu = findViewById(R.id.togbtn_thu)
        togbtn_thu.setOnClickListener(TogOnClickListener)
        togbtn_fri = findViewById(R.id.togbtn_fri)
        togbtn_fri.setOnClickListener(TogOnClickListener)
        togbtn_sat = findViewById(R.id.togbtn_sat)
        togbtn_sat.setOnClickListener(TogOnClickListener)
        togbtn_sun = findViewById(R.id.togbtn_sun)
        togbtn_sun.setOnClickListener(TogOnClickListener)

        btn_set_plus_lock = findViewById(R.id.btn_set_plus_lock)
        btn_set_plus_lock.setOnClickListener(onClickListener)
        btn_set_minus_lock = findViewById(R.id.btn_set_minus_lock)
        btn_set_minus_lock.setOnClickListener(onClickListener)

        val txt_save = findViewById<TextView>(R.id.txt_save)
        txt_save.setOnClickListener(onClickListener)
        val txt_cancle = findViewById<TextView>(R.id.txt_cancle)
        txt_cancle.setOnClickListener(onClickListener)

        txt_lock_time_hour = findViewById(R.id.txt_lock_time_hour)
        txt_lock_time_min = findViewById(R.id.txt_lock_time_min)
        txt_lock_able_hour = findViewById(R.id.txt_lock_able_hour)
        txt_lock_able_min = findViewById(R.id.txt_lock_able_min)

        setInit()
    }

    private fun setInit() {
        val prior = param_dayweek.split("|")

        togbtn_sun.isChecked = "1" == prior.getOrNull(1)
        togbtn_mon.isChecked = "1" == prior.getOrNull(3)
        togbtn_tue.isChecked = "1" == prior.getOrNull(5)
        togbtn_wed.isChecked = "1" == prior.getOrNull(7)
        togbtn_thu.isChecked = "1" == prior.getOrNull(9)
        togbtn_fri.isChecked = "1" == prior.getOrNull(11)
        togbtn_sat.isChecked = "1" == prior.getOrNull(13)

        setLockTime()
        setUsedTime()
    }

    /**
     * 실행 주기 요일 버튼
     */
    private val TogOnClickListener = View.OnClickListener {
        setPrior()
    }

    private fun setPrior() {
        val prior = StringBuilder()
        val offsetX = 0
        val offsetY = 15
        val toast = Toast.makeText(ctx.applicationContext, ctx.getString(R.string.txt_exist_prior), Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.TOP, offsetX, offsetY)
        }

        if (togbtn_sun.isChecked) {
            if (IsExistDayOfWeek(1)) {
                toast.show()
                togbtn_sun.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_mon.isChecked) {
            if (IsExistDayOfWeek(2)) {
                toast.show()
                togbtn_mon.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_tue.isChecked) {
            if (IsExistDayOfWeek(3)) {
                toast.show()
                togbtn_tue.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_wed.isChecked) {
            if (IsExistDayOfWeek(4)) {
                toast.show()
                togbtn_wed.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_thu.isChecked) {
            if (IsExistDayOfWeek(5)) {
                toast.show()
                togbtn_thu.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_fri.isChecked) {
            if (IsExistDayOfWeek(6)) {
                toast.show()
                togbtn_fri.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        if (togbtn_sat.isChecked) {
            if (IsExistDayOfWeek(7)) {
                toast.show()
                togbtn_sat.isChecked = false
                return
            } else {
                prior.append("1|")
            }
        } else {
            prior.append("0|")
        }

        dayweek = prior.toString()
    }

    private fun IsExistDayOfWeek(dayofweek: Int): Boolean {
        if (SkyPriorList.db_prior_list.isNullOrEmpty()) return false

        for (j in 0 until SkyPriorList.db_prior_list.size) {
            if (dayweek == "0|0|0|0|0|0|0|" && j == SkyPriorList.listAdapter_all.getPosition()) continue

            val tmp = isAbleToday(dayofweek, SkyPriorList.db_prior_list[j]["dayweek"].toString())
            if (tmp) {
                return true
            }
        }
        return false
    }

    private fun isAbleToday(today: Int, dayofweek: String): Boolean {
        val dayofweeks = dayofweek.split("|")
        return dayofweeks.getOrNull(today + (today - 1)) == "1"
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.txt_save -> setSave()
            R.id.txt_cancle -> {
                isCancel = true
                cancel()
            }
            R.id.btn_set_plus_lock -> setTime(1)
            R.id.btn_set_minus_lock -> setTime(-1)
        }
    }

    private fun setSave() {
        _cancellistener?.onCancel(this)

        isCancel = false
        cancel()
    }

    private fun setTime(value: Int) {
        var hour = Integer.parseInt(txt_lock_time_hour.text.toString())
        var min = Integer.parseInt(txt_lock_time_min.text.toString())

        if (value > 0) {
            if (min == 1) {
                min = 10
            } else {
                min += 10
            }

            if (min >= 60) {
                hour++
                min = 0

                if (hour >= 24) {
                    hour = 24
                    min = 0
                    return
                }
            }
        } else {
            min -= 10

            if (hour > 0 && min < 0) {
                hour--

                if (hour < 0) {
                    hour = 0
                }

                min = 50
            } else if (hour == 0 && min <= 1) {
                hour = 0
                min = 1
            }
        }

        lmttime = (hour * 60 * 60 * 1000) + (min * 60 * 1000)

        // 잔여시간
        if (txt_lock_time_hour.text == txt_lock_able_hour.text && txt_lock_time_min.text == txt_lock_able_min.text) {
            spctime = (hour * 60 * 60 * 1000) + (min * 60 * 1000)

            txt_lock_able_hour.text = if (hour < 10) "0$hour" else "$hour"
            txt_lock_able_min.text = if (min < 10) "0$min" else "$min"
        }

        // 설정시간
        txt_lock_time_hour.text = if (hour < 10) "0$hour" else "$hour"
        txt_lock_time_min.text = if (min < 10) "0$min" else "$min"
    }

    private fun setLockTime() {
        var hour = 0L
        var min = lmttime

        hour = (min / (1000 * 60 * 60)) % 24
        min = (min / (1000 * 60)) % 60

        txt_lock_time_hour.text = if (hour < 10) "0$hour" else "$hour"
        txt_lock_time_min.text = if (min < 10) "0$min" else "$min"
    }

    private fun setUsedTime() {
        var hour = 0L
        var min = spctime

        hour = (min / (1000 * 60 * 60)) % 24
        min = (min / (1000 * 60)) % 60

        txt_lock_able_hour.text = if (hour < 10) "0$hour" else "$hour"
        txt_lock_able_min.text = if (min < 10) "0$min" else "$min"
    }

    override fun setOnCancelListener(listener: OnCancelListener) {
        _cancellistener = listener
    }

    /**
     * 백버튼이 눌렸을때
     */
    override fun onBackPressed() {
        isCancel = true
        cancel()
    }

    /**
     * 선택한 방법 (0:Cancel, 1:시간제한, 2:앱잠금)
     * @return
     */
    fun getType(): Int {
        return if (isCancel) 0 else 1
    }

    /**
     * 설정된 시간(밀리세컨드)
     * @return
     */
    fun getLmttime(): Long {
        return lmttime
    }

    /**
     * 설정된 남은 시간(밀리세컨드)
     * @return
     */
    fun getSpctime(): Long {
        return spctime
    }

    /**
     * 설정된 요일
     * @return
     */
    fun getDayweek(): String {
        return dayweek
    }
}