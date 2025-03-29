package com.skynet.skytimelock.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.free.R

class SkyBlueLightView : SkyNetBaseActivity() {
    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting
    private lateinit var txt_opacity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(false)
        super.setLayout_Id(R.layout.bluelight_view)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        common = SkyTimeLockCommon(this)
        setting = common.getSetting("SkyTimeLockMainView.loadSetting")

        txt_opacity = findViewById(R.id.txt_opacity)

        val btn_save = findViewById<TextView>(R.id.txt_save)
        btn_save.setOnClickListener(clickListener)

        val btn_cancle = findViewById<TextView>(R.id.txt_cancle)
        btn_cancle.setOnClickListener(clickListener)

        val seekbar = findViewById<SeekBar>(R.id.sbar_opacity)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                txt_opacity.text = progress.toString()
                setting.setBluelight(progress.toString())
                common.setSetting(setting)
                common.setRefreshSetting(true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        seekbar.progress = Integer.parseInt(setting.getBluelight())
    }

    private val clickListener = OnClickListener { arg ->
        when (arg.id) {
            R.id.txt_save -> {
                setting.setBluelight(txt_opacity.text.toString())
                common.setSetting(setting)
                Toast.makeText(applicationContext, getString(R.string.save_ok), Toast.LENGTH_SHORT).show()
                finish()
            }

            R.id.txt_cancle -> {
                finish()
            }
        }
    }
}