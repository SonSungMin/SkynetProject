package com.skynet.framework

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout.LayoutParams

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting

open class SkyNetBaseActivity : Activity() {
    // Log Path
    companion object {
        val LOGCAT_FILE_PATH = "${Environment.getExternalStorageDirectory()}/skynet/log"

        // logcat buffer size
        private const val BUFFER_SIZE = 512

        private const val admob_id = "ca-app-pub-7555366365534166/5513391936" // "a14fe2ffddebb00"
        private const val tad_id = "AX0004619"
        private const val adam_id = "95dbZ0eT1471a5bb814"

        private var marketUpdate = 0

        @SuppressLint("SimpleDateFormat")
        fun diffOfDate(begin: String, end: String): Long {
            var diffDays: Long = 0
            try {
                val formatter = SimpleDateFormat("yyyyMMdd")

                val beginDate = formatter.parse(begin)
                val endDate = formatter.parse(end)

                val diff = endDate.time - beginDate.time
                diffDays = diff / (24 * 60 * 60 * 1000)
            } catch(e: Exception) {
                // 예외 처리
            }

            return diffDays
        }
    }

    // ########### AD #############
    private var admob: com.google.android.gms.ads.AdView? = null

    protected var ad_id: Array<String>? = null
    private var diffDay: Long = -1
    private var isAdApplySuccess = false
    // ############################

    protected lateinit var common: SkyTimeLockCommon
    protected lateinit var main_layout: View
    protected lateinit var main_frame: LinearLayout

    private var add_cnt = 0
    private var layout_id = 0

    private var isNoTitle = true
    private var isAdActivity = false
    private var isTheme = false
    private var isUpdateCheck = false

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        common = SkyTimeLockCommon(this)

        // # 1. 가장 외곽의 Layout
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        main_frame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = params
        }

        // # 2. Layout Xml
        val inflater_main_layout = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        main_layout = inflater_main_layout.inflate(layout_id, null)
        val main_params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1f
        }
        main_layout.layoutParams = main_params

        // 메인 뷰에 추가
        addMainView(main_layout, main_params)

        if (isAdActivity) {
            try {
                // # 3. 메인과 AD의 간격 유지를 위한 FrameLayout
                val space_layout = FrameLayout(this)
                val spaceParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0)
                space_layout.layoutParams = spaceParams

                // 메인 뷰에 추가
                main_frame.addView(space_layout, add_cnt++, spaceParams)

                // 광고 뷰 생성
                startAD()
            } catch(e: Exception) {
                setLog("광고 호출 오류 : $e")
            }
        }

        if (marketUpdate == 0 && isUpdateCheck)
            marketUpdate()

        marketUpdate++

        super.setContentView(main_frame)
    }

    /**
     * 마켓 업데이트
     */
    fun marketUpdate() {
        try {
            if (common.isKor())
                SkyNetMarketUpdate(this).execute("http://1-dot-smarttimeguide.appspot.com/timeguide.html", null, null)
            else
                SkyNetMarketUpdate(this).execute("http://1-dot-smarttimeguide.appspot.com/timeguide_en.html", null, null)
        } catch(e: Exception) {
            // 예외 처리
        }
    }

    protected fun setApplyTheme(b: Boolean) {
        this.isTheme = b
    }

    /**
     * 바인딩할 View
     * @param layoutid
     */
    protected fun setLayout_Id(layoutid: Int) {
        this.layout_id = layoutid
    }

    /**
     * 타이틀 표시 여부
     * @param v
     */
    protected fun setNoTitle(v: Boolean) {
        this.isNoTitle = v
    }

    /**
     * AD Activity 여부
     * @param v
     */
    protected fun setAdActivity(v: Boolean) {
        this.isAdActivity = v
    }

    protected fun setUpdateCheck(v: Boolean) {
        this.isUpdateCheck = v
    }

    /**
     * 뷰 추가
     */
    private fun addMainView(view: View) {
        main_frame.addView(view, add_cnt++)
    }

    private fun addMainView(view: View, ad_params: LayoutParams) {
        main_frame.addView(view, add_cnt++, ad_params)
    }

    private fun addMainView(view: View, ad_params: LinearLayout.LayoutParams) {
        main_frame.addView(view, add_cnt++, ad_params)
    }

    /**
     * 뷰 삭제
     * @param view
     */
    private fun removeView(view: View) {
        main_frame.removeView(view)
    }

    /**
     * Progress 보이기
     */
    protected fun showWaitProg() {
        showWaitProg(null)
    }

    /**
     * Progress 보이기
     * @param msg
     */
    protected fun showWaitProg(msg: String?) {
        progressDialog = ProgressDialog.show(this, "", if (msg == null || msg.isEmpty()) "Please Wait..." else msg, true, true)
    }

    /**
     * Progress 숨기기
     */
    protected fun hideWaitProg() {
        progressDialog?.dismiss()
    }

    /**
     * Log 기록
     * @param log
     */
    protected fun setLog(log: String) {
        common.setLogMsg(log)
    }

    /**
     * LogCat Log 기록
     */
    protected fun makeLogCat() {
        showWaitProg()

        val cmd = "logcat"
        val option = "-d"
        val tag = "*:E SkyNet:* *:S" // "*:E AndroidRuntime:* System.err:*"

        val LOGCAT_CMD = arrayOf(cmd, option, tag)

        var logcatProc: Process? = null

        try {
            logcatProc = Runtime.getRuntime().exec(LOGCAT_CMD)
        } catch (e: IOException) {
            setLog(e.toString())
        }

        var reader: BufferedReader? = null
        val lineSeparator = System.getProperty("line.separator")

        val strOutput = StringBuilder()

        var info: PackageInfo? = null
        try {
            info = packageManager.getPackageInfo(packageName, 0)
        } catch (e1: NameNotFoundException) {
            setLog("$e1")
        }

        strOutput.append("\n")

        if (info != null) {
            strOutput.append("App Version : ")
            strOutput.append(info.versionName)
            strOutput.append("\n")
        }

        strOutput.append("App Language : ")
        if (common.isKor())
            strOutput.append("한국어\n")
        else
            strOutput.append("English\n")

        strOutput.append("Model : ")
        strOutput.append(Build.MODEL)
        strOutput.append("\n")

        strOutput.append("Android Ver. : ")
        strOutput.append(Build.VERSION.SDK_INT)
        strOutput.append("\n\n")

        val setting = common.getSetting("baseActivity.MakeLogCat")

        strOutput.append("------------- Setting --------------\n")
        strOutput.append("방법(1앱별,2일일) : ${setting.getLock_mode()}")
        strOutput.append("\n")
        strOutput.append("유형(1고장,2비번,3패턴) : ${setting.getMode()}")
        strOutput.append("\n")
        strOutput.append("주기 : ${setting.getPrior_value()}")
        strOutput.append("\n")
        strOutput.append("SMS Lock : ${setting.getSmslock()}")
        strOutput.append("\n")
        strOutput.append("SMS UnLock : ${setting.getSmsunlockmsg()}")
        strOutput.append("\n")
        strOutput.append("Icon : ${setting.getStatusicon()}")
        strOutput.append("\n")
        strOutput.append("Option1(남은시간표시) : ${setting.isOption1()}")
        strOutput.append("\n")
        strOutput.append("Option2(설정화면 잠금) : ${setting.isOption2()}")
        strOutput.append("\n")
        strOutput.append("Option3(무작위 배경) : ${setting.isOption3()}")
        strOutput.append("\n")
        strOutput.append("Option4(잠금활성화) : ${setting.isOption4()}")
        strOutput.append("\n")
        strOutput.append("Option5(삭제보호) : ${setting.isOption5()}")
        strOutput.append("\n")
        strOutput.append("Option6(데몬설치) : ${setting.isOption6()}")
        strOutput.append("\n")
        strOutput.append("Option7(자동시작) : ${setting.isOption7()}")
        strOutput.append("\n")
        strOutput.append("Option8 : ${setting.isOption8()}")
        strOutput.append("\n")
        strOutput.append("Option9(신규설치앱 잠금) : ${setting.isOption9()}")
        strOutput.append("\n")
        strOutput.append("Option10(잠금활성시간설정) : ${setting.isOption10()}")
        strOutput.append("\n")

        strOutput.append("\n\n\n")

        try {
            reader = BufferedReader(InputStreamReader(logcatProc?.inputStream), BUFFER_SIZE)

            var line: String?

            while (reader.readLine().also { line = it } != null) {
                strOutput.append(line)
                strOutput.append(lineSeparator)
            }

            reader.close()
        } catch (e: IOException) {
            setLog(e.toString())
        }

        hideWaitProg()

        strOutput.append("\n\n\n")
        strOutput.append("-------------------------------\n")
        if (common.isKor())
            strOutput.append("증상을 적어주세요 : ")
        else
            strOutput.append("Your Message : ")
        strOutput.append("\n\n\n")

        // 로그 전송
        sendEmailOrMessage(strOutput.toString())
    }

    /**
     * 로그파일 전송
     */
    protected fun sendEmailOrMessage(msg: String) {
        val i = Intent(Intent.ACTION_SEND).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "$packageName Log File")
            putExtra(Intent.EXTRA_TEXT, msg) // 본문
            putExtra(Intent.EXTRA_EMAIL, arrayOf("ssm7777777@gmail.com")) // 받는사람
            putExtra(Intent.EXTRA_CC, arrayOf("")) // 보내는사람
            putExtra(Intent.EXTRA_BCC, arrayOf("")) // 숨은참조
        }
        startActivity(Intent.createChooser(i, "Choose Email Client"))
    }

    // ################################################################################################
    // ######################################  AD  ####################################################
    // ################################################################################################

    override fun onDestroy() {
        try
        {
            admob?.apply()
            {
                removeAllViews()
                destroy()
                admob = null
            }
        } catch(e: Exception) {
            setLog("AD onDestroy $e")
        }

        super.onDestroy()
    }

    @SuppressLint("SimpleDateFormat")
    @TargetApi(Build.VERSION_CODES.FROYO)
    protected fun startAD() {
        val ad_params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100)
        val ad_frame = FrameLayout(this.applicationContext)
        val fparams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        ad_frame.setBackgroundColor(Color.TRANSPARENT)
        ad_frame.layoutParams = fparams

        // 국내 유저용
        if (common.isKor())
        {
            admob = com.google.android.gms.ads.AdView(this).apply {
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                adUnitId = admob_id
            }
            admob?.layoutParams = ad_params

            ad_frame.addView(admob)

            admob?.visibility = View.INVISIBLE

            addMainView(ad_frame, ad_params)
        }
        else
        {
            admob = com.google.android.gms.ads.AdView(this).apply {
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                adUnitId = admob_id
                layoutParams = ad_params
            }
            ad_frame.addView(admob)
            admob?.visibility = View.INVISIBLE

            addMainView(ad_frame, ad_params)
            initAdmob()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun isInDate(begin: String, end: String): Boolean {
        isAdApplySuccess = false
        try {
            val formatter = SimpleDateFormat("yyyyMMdd")

            val beginDate = begin.toInt()
            val endDate = end.toInt()
            val toDay = formatter.format(Date()).toInt()

            if (toDay in beginDate..endDate)
                isAdApplySuccess = true
        } catch(e: Exception) {
            // 예외 처리
        }

        return isAdApplySuccess
    }

    private fun visibleAdMob() {
        admob?.apply {
            bringToFront()
            visibility = View.VISIBLE
        }
    }

    private fun inVisibleAdMob() {
        admob?.visibility = View.INVISIBLE
    }

    // AdMob
    protected fun initAdmob() {
        if (admob == null) {
            admob = com.google.android.gms.ads.AdView(this).apply {
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                adUnitId = admob_id
            }
        }

        val adRequest = AdRequest.Builder().build()

        admob?.adListener = admobAdListener
        admob?.loadAd(adRequest)

        visibleAdMob()
    }


    // AdMob
    private val admobAdListener = object : com.google.android.gms.ads.AdListener() {
        override fun onAdLoaded() {
            visibleAdMob()
        }

//        override fun onAdFailedToLoad(p0: LoadAdError) {
//            inVisibleAdMob()
//        }
    }
}