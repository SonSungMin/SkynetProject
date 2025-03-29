package com.skynet.skytimelock.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.skynet.common.SkyTimeLockCommon
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.free.R

class SkyLockViewWiFi : SkyNetBaseActivity() {
    private lateinit var btn_wifi_yes: Button
    private lateinit var btn_wifi_no: Button
    private lateinit var layout_main: LinearLayout

    private var wifi_pkgnme: String? = null

    private lateinit var timeguide_title_img: ImageView
    private lateinit var txt_wifi_title: TextView

    private lateinit var wifiConnectThread: Thread

    private lateinit var common: SkyTimeLockCommon

    private var threadRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(true)
        super.setLayout_Id(R.layout.lock_wifi)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        common = SkyTimeLockCommon(this)

        layout_main = findViewById(R.id.linear_layout_wifi_view)

        wifi_pkgnme = intent.getStringExtra("PKGNME")

        btn_wifi_yes = findViewById(R.id.btn_wifi_yes)
        btn_wifi_no = findViewById(R.id.btn_wifi_no)

        btn_wifi_yes.setOnClickListener(clickListener)
        btn_wifi_no.setOnClickListener(clickListener)

        common.setIsCheckedWiFi(false)

        txt_wifi_title = findViewById(R.id.txt_wifi_title)
        txt_wifi_title.setShadowLayer(1f, 1f, 1f, Color.BLACK)

        timeguide_title_img = findViewById(R.id.img_wifi_app_icon)

        val pm = this.packageManager
        var icon: Drawable? = null
        try {
            icon = pm.getApplicationIcon(wifi_pkgnme!!)
            timeguide_title_img.setImageDrawable(icon)
        } catch (e: NameNotFoundException) {
            // Ignore exception
        }

        wifiConnectThread = Thread(wifiRunnable)
        wifiConnectThread.start()
    }

    private val wifiRunnable = Runnable {
        try {
            while (threadRunning) {
                // WiFi에 접속되지 않은 경우 접속 시도
                if (!checkWifi()) {
                    val mwifi = getSystemService(Context.WIFI_SERVICE) as WifiManager
                    mwifi.isWifiEnabled = true
                } else {
                    finish()
                }

                SystemClock.sleep(1000)
            }
        } catch (e: Exception) {
            common.setLogMsg(e.message ?: "")
        }
    }

    private fun checkWifi(): Boolean {
        val cManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        return wifi?.isConnected ?: false
    }

    private val clickListener = OnClickListener { arg ->
        when (arg.id) {
            R.id.btn_wifi_yes -> {
                // 와이파이를 켜지 않아도 앱을 실행한다는 버튼을 눌렀을 경우 패키지명을 저장한다.
                common.setIsCheckedWiFi(true)
                interruptThread()

                finish()
            }

            R.id.btn_wifi_no -> {
                common.setIsCheckedWiFi(false)
                interruptThread()

                val i = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_HOME)
                }
                startActivity(i)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        return
    }

    @Suppress("DEPRECATION")
    override fun onStart() {
        layout_main.background = common.getBackgroundImg()
        super.onStart()
    }

    override fun onDestroy() {
        interruptThread()

        super.onDestroy()
    }

    private fun interruptThread() {
        if (::wifiConnectThread.isInitialized && wifiConnectThread.isAlive) {
            wifiConnectThread.interrupt()
            threadRunning = false
        }
    }
}