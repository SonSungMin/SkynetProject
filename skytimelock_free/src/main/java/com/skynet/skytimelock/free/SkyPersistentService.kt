package com.skynet.skytimelock.free

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.skytimelock.view.SkyDialogActivity
import com.skynet.skytimelock.view.SkyLockPatternView
import com.skynet.skytimelock.view.SkyLockView
import com.skynet.skytimelock.view.SkyLockViewWiFi
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap

@SuppressLint("HandlerLeak")
class SkyPersistentService : Service() {

    companion object {
        // View components
        var bluelightView: LinearLayout? = null
        var bluelightWm: WindowManager? = null
        var bluelight_params: WindowManager.LayoutParams? = null
        var bluelightValue = 0.3f

        var timerView: TextView? = null // 타이머 뷰
        var START_TIMER = false
        var wm: WindowManager? = null
        var params: WindowManager.LayoutParams? = null
        var STATUS_ICON: String? = null

        val noti_id = 7777777
        var nm: NotificationManager? = null
        var mNotification: Notification? = null
        var pendingIndent: PendingIntent? = null
        var icon = 0

        // 홈런처
        var homeApps: List<ResolveInfo>? = null
        var mainIntent: Intent? = null

        var demonThread: Thread? = null
        var lockThread: Thread? = null
        var packageThread: Thread? = null

        var threadRunning = true
        var demonThreadRunning = true

        var targetIndex = 0
        var updatedFinishCnt = 0

        val ACTION = "action.service.SkyTimeLock"
        val ACTION_DEMON = "action.service.SkyTimeLockHelper"

        /**
         * 일일 사용시간 _ 허용앱인지 여부
         */
        var isDailyAbleApp = false

        /**
         * 남은 시간 표시 여부
         */
        var sPref_option1 = false

        /**
         * 잠금 활성화 여부
         */
        var sPref_option4 = false

        /**
         * 어플 추가/삭제시 암호로 보호
         */
        var sPref_option5 = false

        /**
         * 블루 라이트
         */
        var sPref_option6 = false

        /**
         * 일일 시간 제한인 경우
         */
        var sPref_timeMode = false

        /**
         * 시간제한 및 락 어플 여부
         */
        var IsTargetApp = false

        /**
         * sms수신시 모두 잠금
         */
        var isSMSAllLock = false

        var isFirstOn = true
        var isFirstOff = true
        var isOnWiFi = false

        /**
         * 잠금 화면 종류 (1:고장화면, 2:비번, 3:패턴)
         */
        var lock_mode: String? = null

        /**
         * 잠금화면 mode (고장,비번,패턴)
         */
        var sPref_mode: String? = null

        /**
         * 앱 실행 후 앱이 최초로 실행했는지 확인 flag(0이면 최초 실행)
         */
        var sPref_step = 0
        var sPref_step_his = 0
        var TODAY: String? = null
        var pre_exetime = System.currentTimeMillis()

        /**
         * 어플 실행일
         */
        var app_stdte: String? = null

        /**
         * 앱 실행 시작 시간
         */
        var exe_start_time: Long = 0
        var exe_start_time_his: Long = 0

        /**
         * 자동 종료 시작 시간
         */
        var starttime: Long = 0

        /**
         * 자동 종료 시작 시간 _ 사용시간
         */
        var starttime_his: Long = 0

        /**
         * 사용가능 저장 시간
         */
        var lmttime: Long = 0

        /**
         * 사용시간
         */
        var spctime: Long = 0

        /**
         * 사용가능 시간
         */
        var usabletime: Long = 0

        /**
         * 시작 시간_사용기록
         */
        var history_starttime: Long = 0

        var info: List<ActivityManager.RunningTaskInfo>? = null

        /**
         * 일일 설정된 요일
         */
        var dayWeek: String? = null

        /**
         * 앱별 설정된 요일
         */
        var dayWeekApp: String? = null
        var pre_packageName = "1"
        var packageName: String? = null
        var className: String? = null

        /**
         * 어플 구분 (1:시간제한,2:잠금)
         */
        var exe_gbn: String? = null

        /**
         * 화면이 켜졌는지
         */
        var isScreenOn = true
        var isScreenOn_his = true

        /**
         * 잠금 어플, 시간제한 어플 리스트
         */
        var select_list: ArrayList<HashMap<Any, Any>>? = null

        /**
         * 허용가능 앱 리스트
         */
        var allow_list: ArrayList<HashMap<Any, Any>>? = null

        /**
         * 사용 통계 리스트
         */
        var count_list: ArrayList<HashMap<Any, Any>>? = null

        /**
         * 일일 시간제한 설정 리스트
         */
        var prior_list: ArrayList<HashMap<Any, Any>>? = null

        @SuppressLint("SimpleDateFormat")
        val formatter = SimpleDateFormat("yyyy-MM-dd")

        var mActivityManager: ActivityManager? = null
        var dbm: DbManager? = null
        var common_service: SkyTimeLockCommon? = null
        var setting_service: SkyTimeLockSetting? = null

        var cManager: ConnectivityManager? = null
        var isDemonRunning = false

        var SCREEN_ON = true
    }

    private var unlockedPkgname = ""
    private var on_off_Receiver: BroadcastReceiver? = null
    private var sky_km: KeyguardManager? = null

    @Suppress("DEPRECATION")
    private var sky_keyLock: KeyguardManager.KeyguardLock? = null

    /**
     * 자동저장 시간(30초)
     */
    private val auto_save: Long = 30 * 1000

    @SuppressLint("NewApi", "ServiceCast")
    override fun onCreate() {
        super.onCreate()
        init()
    }

    // 서비스를 새로 시작하거나, startService를 호출할 경우 실행
    @Deprecated("Deprecated in Java")
    override fun onStart(intent: Intent?, startId: Int) {
        if (startId == 1) {
            common_service?.setRefreshSetting(true)
            common_service?.setIsInit(true)
            init()
        }
        super.onStart(intent, startId)
    }

    // 서비스를 새로 시작하거나, startService를 호출할 경우 실행
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId == 1) {
            common_service?.setRefreshSetting(true)
            common_service?.setIsInit(true)
            init()
        }
        return START_STICKY
    }

    /**
     * 초기화
     */
    @Suppress("DEPRECATION")
    private fun init() {
        if (sky_km == null) sky_km = getSystemService(Activity.KEYGUARD_SERVICE) as KeyguardManager
        if (sky_keyLock == null) sky_keyLock = sky_km?.newKeyguardLock("SkyNet_KeyLock")
        if (dbm == null) dbm = DbManager(this)
        if (common_service == null) common_service = SkyTimeLockCommon(this.applicationContext)
        if (homeApps == null) {
            mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            homeApps = packageManager.queryIntentActivities(
                mainIntent!!,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            )
        }

        if (common_service?.getIsInit() != true) return

        common_service?.setRefreshSetting(true)
        common_service?.setScreenOn(true)

        common_service?.setLogMsg("Init")

        getSetting("mainService.getSetting.init")

        // 상태바 아이콘 설정 및 포그라운드 서비스 등록
        initStatus()

        // Screen On, Off 리시버 등록
        if (on_off_Receiver == null) {
            val filter = IntentFilter(Intent.ACTION_SCREEN_ON).apply {
                addAction(Intent.ACTION_SCREEN_OFF)
            }

            on_off_Receiver = SkyTimeLockScreenOnOffBroadcast()
            registerReceiver(on_off_Receiver, filter)
        }

        threadRunning = true
        demonThreadRunning = true

        lockThread = Thread(lockRunnable)
        lockThread?.start()

        demonThread = Thread(demonRunnable)
        demonThread?.start()

        packageThread = Thread(packageRunnable)
        packageThread?.start()

        common_service?.setIsInit(false)
    }

    @Suppress("DEPRECATION")
    private fun initStatus() {
        common_service?.setLogMsg("Init StatusBar")

        // Status 아이콘
        icon = 0
        STATUS_ICON = common_service?.getStatusIcon()

        when (STATUS_ICON) {
            "1" -> icon = R.drawable.status1
            "2" -> icon = R.drawable.status2
            "3" -> icon = R.drawable.status3
            "4" -> icon = R.drawable.status4
            "5" -> icon = R.drawable.status5
            "6" -> icon = R.drawable.status6
            "7" -> icon = R.drawable.status7
            "8" -> icon = R.drawable.status8
            "9" -> icon = R.drawable.status9
            "10" -> icon = R.drawable.status10
            "11" -> icon = R.drawable.status11
            "12" -> icon = R.drawable.status12
        }

        if (nm == null) nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (STATUS_ICON == "13") {
            // 아이콘 없음
            mNotification = Notification(0, null, System.currentTimeMillis()).apply {
                flags = flags or Notification.FLAG_NO_CLEAR
            }
            startForeground(noti_id, mNotification)
            nm?.cancel(noti_id)
        } else {
            pendingIndent = PendingIntent.getActivity(
                applicationContext, 0,
                Intent(this, SkyTimeLockActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )
            mNotification =
                Notification(icon, getString(R.string.app_status), System.currentTimeMillis())
            mNotification?.setLatestEventInfo(
                this,
                getString(R.string.app_name),
                getTodayUsed(),
                pendingIndent
            )
            startForeground(noti_id, mNotification)
        }
    }

    private fun destoryThread() {
        try {
            if (lockThread != null && lockThread!!.isAlive) {
                threadRunning = false
                lockThread?.interrupt()
            }

            if (demonThread != null && demonThread!!.isAlive) {
                demonThreadRunning = false
                demonThread?.interrupt()
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    /**
     * 설정 정보 가져오기
     */
    private fun getSetting(gbn: String) {
        if (common_service?.getRefreshSetting() != true) return

        setting_service = common_service?.getSetting(gbn)

        initStatus()

        // 잠금화면 mode (고장,비번,패턴)
        sPref_mode = setting_service?.getMode()
        // 남은 시간 표시 여부
        sPref_option1 = setting_service?.isOption1() ?: false
        // 잠금활성화 여부
        sPref_option4 = setting_service?.isOption4() ?: false
        // 어플 추가/삭제 보호
        sPref_option5 = setting_service?.isOption5() ?: false
        // 블루 라이트
        sPref_option6 = setting_service?.isOption6() ?: false
        // 잠금 방법 (앱별/일일)
        sPref_timeMode = setting_service?.getLock_mode().equals("2")
        // sms수신시 모두 잠금
        isSMSAllLock = common_service?.getAllLock() ?: false
        // 앱별 잠금 요일 설정
        dayWeekApp = setting_service?.getPrior_value()

        bluelightValue = setting_service?.getBluelight()?.toFloat()?.div(150) ?: 0.3f

        try {
            select_list = dbm?.executeSelect("SELECT * FROM applist", null)
            allow_list = dbm?.executeSelect("SELECT * FROM allowedlist", null)
            prior_list = dbm?.executeSelect("SELECT * FROM priorlist", null)
        } catch (e: Exception) {
            common_service?.setLogMsg("[packageRunnable.설정 조회]${e}")
        }

        common_service?.setRefreshSetting(false)
    }

    /**
     * 데몬 스레드
     */
    private val demonRunnable = Runnable {
        try {
            while (demonThreadRunning) {
                // 데몬 살리기
                if (common_service?.getScreenOn() == true) {
                    startService(Intent(ACTION_DEMON))
                }

                SystemClock.sleep(500)
            }
        } catch (e: Exception) {
            common_service?.setLogMsg("demonRunnable : $e")
        }
    }

    /**
     * 패키지 명과 설정을 가져오는 쓰레드
     */
    private val packageRunnable = Runnable {
        try {
            // 초기화
            common_service?.setIsCheckedPwd(false)
            common_service?.setIsCheckedWiFi(false)

            cManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            while (threadRunning) {
                SystemClock.sleep(200)

                // 비활성화 여부
                if ((!isSMSAllLock && !sPref_option4)) continue

                // 현재 실행중인 패키지명 조회
                info = mActivityManager?.getRunningTasks(1)
                if (info == null) {
                    mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    continue
                }

                // 현재 실행중인 패키지명
                packageName = info!![0].topActivity.packageName
                className = info!![0].topActivity.className

                // 타임가이드 삭제 방지
                if (sPref_option5 && info!![0].topActivity.className == "com.android.settings.DeviceAdminAdd") {
                    if (!common_service!!.getIsCheckedPwd()) {
                        lock_mode = sPref_mode
                        showLockView(true, true)
                    }
                    sPref_step = 0
                    continue
                }
            }
        } catch (e: Exception) {
            common_service?.setLogMsg("packageRunnable : $e")
        }
    }

    /**
     * 메인 스레드
     */
    private val lockRunnable = Runnable {
        while (threadRunning) {
            synchronized(ACCESSIBILITY_SERVICE) {
                SystemClock.sleep(300)

                // 비활성화 여부
                if (packageName == null || (!isSMSAllLock && !sPref_option4)) continue

                // 설정 가져오기
                getSetting("mainService.getSetting.lockRunnable")

                // BlueLight
                if (SCREEN_ON) {
                    mHandler.sendMessage(Message.obtain(mHandler, 11))
                } else {
                    stopBlueLight("SCREEN_ON")
                }

                // 시간 변경
                if (className == "com.android.settings.Settings\$DateTimeSettingsActivity") {
                    if (!common_service!!.getIsCheckedPwd()) {
                        showLockView(true, false)
                        lock_mode = sPref_mode
                    }
                    sPref_step = 0
                    continue
                }

                // 타임가이드 삭제 방지
                if (sPref_option5 && className == "com.android.settings.DeviceAdminAdd") {
                    if (!common_service!!.getIsCheckedPwd()) {
                        lock_mode = sPref_mode
                    }
                    sPref_step = 0
                    continue
                }

                // 락 화면이 실행중인 경우 Process 를 타지 않는다.
                // 전화 기능도 동일
                if (packageName!!.startsWith("com.skynet.skytimelock") ||
                    packageName!!.startsWith("com.android.phone") ||
                    packageName!!.startsWith("com.android.contact")
                ) {
                    stopTimerService("STEP 1")

                    IsTargetApp = getIsTargetApp(packageName!!)

                    if (sPref_timeMode || IsTargetApp) {
                        spctime = usabletime
                        exe_start_time = System.currentTimeMillis()
                    }
                    sPref_step = 0
                    continue
                }

                // SMS 수신 전체 잠금인 경우 잠금 앱으로 강제 변경
                if (isSMSAllLock) {
                    // 타이머 뷰 중지
                    stopTimerService("STEP 3 ")
                    sPref_step = 0
                    common_service?.setRefreshSetting(false)

                    // 비번 입력 화면 실행 조건
                    // 암호를 입력하지 않은 경우 또는 잘못 입력된 경우 (isCheckedPwd == false)
                    if (!common_service!!.getIsCheckedPwd()) {
                        unlockedPkgname = packageName!!
                        showLockView(true, false)
                    } else {
                        common_service?.setAllLock(false)
                        common_service?.setRefreshSetting(true)
                    }

                    // 일일 시간 제한인 경우
                    if (sPref_timeMode) {
                        stopTimerService("STEP 11")
                        updateFinishInfo()
                    }
                    continue
                }

                SCREEN_ON = common_service!!.getScreenOn()
                // 자동 종료 시작 시간
                starttime = System.currentTimeMillis()
                // 어플 시작일(yyyy-MM-dd)
                app_stdte = common_service?.getLockDate()

                // 사용 기록
                // 화면이 꺼지면
                if (!SCREEN_ON) {
                    if (isScreenOn_his) {
                        updateFinishInfo()
                    }
                    isScreenOn_his = false
                } else {
                    // 오늘 날짜는 메인 프로세스가 시작되기 전 날짜를 사용한다.
                    TODAY = formatter.format(Date(System.currentTimeMillis()))
                    // 날짜가 변경됐을 경우
                    if (TODAY != app_stdte) {
                        sPref_step_his = 0
                    }

                    // 화면이 꺼졌다 다시 켜진경우 어플 시작시간을 켜진시간으로 초기화한다.
                    if (!isScreenOn_his || sPref_step_his == 0) {
                        starttime_his = System.currentTimeMillis()
                        exe_start_time_his = System.currentTimeMillis()
                    }
                    isScreenOn_his = true
                    sPref_step_his = 1
                }

                // 앱별 실행
                if (!sPref_timeMode) {
                    IsTargetApp = getIsTargetApp(packageName!!)

                    // 현재 실행중인 앱이 락 대상일 경우만 프로세스 실행
                    if (IsTargetApp && isAbleToday(dayWeekApp)) {
                        updatedFinishCnt = 0

                        // 화면이 꺼지면
                        if (!SCREEN_ON) {
                            // 타이머 뷰 종료
                            stopTimerService("STEP 5")

                            // exe_gbn = 1 실행 시간 제한 앱
                            if (isScreenOn && "1" == exe_gbn) {
                                updateFinishInfo()
                            }

                            isScreenOn = false
                        }
                        // 화면이 켜지면
                        else {
                            // 화면이 꺼졌다 다시 켜진경우 어플 시작시간을 켜진시간으로 초기화한다.
                            if (!isScreenOn) {
                                spctime = usabletime
                                exe_start_time = System.currentTimeMillis()

                                if ("1" == exe_gbn) {
                                    startTimerService("STEP 1")
                                }
                            }

                            isScreenOn = true

                            app_stdte = select_list!![targetIndex]["app_stdte"].toString()

                            // 오늘 날짜는 메인 프로세스가 시작되기 전 날짜를 사용한다.
                            TODAY = formatter.format(Date(System.currentTimeMillis()))
                            // 날짜가 변경됐을 경우
                            if (TODAY != app_stdte) {
                                common_service?.setRefreshSetting(true)
                                // 시간 설정을 초기화하기 위해
                                sPref_step = 0
                            }

                            // 락 프로세스 실행
                            startTimeLock()
                        }
                    } else {
                        sPref_step = 0
                        // 타이머 뷰 종료
                        stopTimerService("STEP 7")
                    }
                }
                // 일일 시간제한
                else if (sPref_timeMode) {
                    isDailyAbleApp = getIsAllowTimeLockApp()

                    // 현재 실행중인 앱이 항상 실행 가능 어플이 아니고,
                    // 오늘이 잠금 대상 요일로 설정된 경우만 프로세스 실행
                    if (!isDailyAbleApp) {
                        updatedFinishCnt = 0

                        // 화면이 꺼지면
                        if (!SCREEN_ON) {
                            // 타이머 뷰 종료
                            stopTimerService("STEP 2")

                            if (isScreenOn) {
                                updateFinishInfo()
                            }

                            isScreenOn = false
                        }
                        // 화면이 켜지면
                        else {
                            // 화면이 꺼졌다 다시 켜진경우 어플 시작시간을 켜진시간으로 초기화한다.
                            if (!isScreenOn) {
                                spctime = usabletime
                                exe_start_time = System.currentTimeMillis()
                                startTimerService("STEP 2")
                            }

                            isScreenOn = true

                            // 오늘 날짜는 메인 프로세스가 시작되기 전 날짜를 사용한다.
                            TODAY = formatter.format(Date(System.currentTimeMillis()))
                            // 날짜가 변경됐을 경우
                            if (TODAY != app_stdte) {
                                common_service?.setRefreshSetting(true)
                                // 시간 설정을 초기화하기 위해
                                sPref_step = 0
                            }

                            // 락 프로세스 실행
                            startTimeLockApp()
                        }
                    } else {
                        sPref_step = 0
                        // 타이머 뷰 종료
                        stopTimerService("STEP 4")
                    }
                }
                mHandler.sendMessage(Message.obtain(mHandler, 10))
                // 종료 처리
                finishProcess()
            }
        }
    }

    private fun getHome(pkgnme: String): Boolean {
        val manager = packageManager

        homeApps?.forEach { resolveInfo ->
            if (resolveInfo.loadLabel(manager) != null || resolveInfo.loadLabel(manager).toString()
                    .trim().isNotEmpty()
            ) {
                val pkgname = resolveInfo.activityInfo.packageName.trim()
                if (pkgname.startsWith(pkgnme)) return true
            }
        }

        return false
    }

    /**
     * 앱별 시간 제한 프로세스
     */
    @Synchronized
    private fun startTimeLock() {
        exe_gbn = select_list!![targetIndex]["exe_gbn"].toString()
        lock_mode = select_list!![targetIndex]["option1"].toString()
        isOnWiFi = select_list!![targetIndex]["option2"] != null &&
                "Y" == select_list!![targetIndex]["option2"].toString()

        // 현재 실행중인 앱이 타임락 대상이면
        if ("1" == exe_gbn && getIsDayOfWeekApp()) {
            // 타이머뷰 시작
            startTimerService("STEP 3")

            // 새로운 앱이 실행되고 초기 한번 앱 사용시간 초기화 설정
            if (sPref_step == 0) {
                select_list = dbm?.executeSelect("SELECT * FROM applist", null)

                // 남은 시간
                lmttime = select_list!![targetIndex]["lmttime"].toString().toLong()
                app_stdte = select_list!![targetIndex]["app_stdte"].toString()

                // 날짜가 변경됐을 경우
                if (TODAY != app_stdte) {
                    // 시간을 임의 변경하지 않은 경우만 남은 시간을 설정시간으로 초기화
                    spctime = lmttime

                    val whereClause = "pkgnme=?"
                    val values = HashMap<Any, Any>()
                    values["app_stdte"] = TODAY!!
                    dbm?.executeUpdate("applist", values, whereClause, arrayOf(packageName!!))

                    common_service?.setLockDate()

                    WriteLog("날짜 변경 시간 초기화")
                } else {
                    spctime = when {
                        select_list!![targetIndex]["spctime"] == null -> 0
                        "null" == select_list!![targetIndex]["spctime"].toString() -> 0
                        else -> select_list!![targetIndex]["spctime"].toString().toLong()
                    }
                }

                exe_start_time = System.currentTimeMillis()

                WriteLog("시간 조회")
            }

            // 패스워드 입력을 성공했으면, 사용시간 초기화
            if (common_service!!.getIsCheckedPwd() && !isLockApp()) {
                spctime = lmttime
                exe_start_time = System.currentTimeMillis()
                common_service?.setIsCheckedPwd(false)
                common_service?.setLockDate()
                WriteLog("패스워드 입력_사용시간 초기화")

                // 사용시간 저장
                val values = HashMap<Any, Any>()
                values["exe_eddte"] = exe_start_time
                values["spctime"] = spctime
                val whereClause = "pkgnme=?"

                dbm?.executeUpdate("applist", values, whereClause, arrayOf(packageName!!))
            }

            // 사용 가능 시간 = 남은 시간 - (어플 실행시간 - 현재시간)
            usabletime = spctime - (System.currentTimeMillis() - exe_start_time)

            sPref_step = 1

            if (showWiFiView()) return

            if (usabletime <= 0) {
                // 타이머 뷰 중지
                stopTimerService("STEP 8")

                unlockedPkgname = packageName!!
                usabletime = 0

                // 시간 초기화
                if ("1" == exe_gbn) {
                    WriteLog("소진 완료 시간 저장")

                    // 사용 가능 시간 기록
                    val values = HashMap<Any, Any>()
                    values["exe_eddte"] = System.currentTimeMillis()
                    values["spctime"] = 0
                    val whereClause = "pkgnme=?"

                    dbm?.executeUpdate("applist", values, whereClause, arrayOf(packageName!!))
                }

                // 잠금 화면 호출
                showLockView()
                sPref_step = 0
            }
        }
        // 잠금 어플인 경우
        else if ("2" == exe_gbn) {
            setLockApp()
        }
    }

    /**
     * 일일 사용시간 제한
     */
    @Synchronized
    private fun startTimeLockApp() {
        try {
            // 해당 앱이 잠금 앱인 경우
            if (getIsTargetLockApp()) {
                exe_gbn = "2"
                lock_mode = select_list!![targetIndex]["option1"].toString()
            } else {
                exe_gbn = "9"
                lock_mode = setting_service?.getMode()
            }

            // 잠금 어플인 경우
            if ("2" == exe_gbn) {
                setLockApp()
                sPref_step = 1
            }
            // 현재 실행중인 앱이 타임락 대상이면
            else if (getIsDayOfWeek()) {
                startTimerService("STEP 5")

                // 기본정보 조회
                // - 앱이 실행된 후 최초 한번
                if (sPref_step == 0) {
                    // 설정된 사용 시간, 어플 시작일 가져오기
                    // lmttime, dayWeek 를 조회한다.
                    getDayOfWeekSetting()

                    // 날짜가 변경됐을 경우
                    if (TODAY != app_stdte) {
                        spctime = lmttime
                        // 변경된 날짜를 app_stdte로 맞추기 위해
                        common_service?.setLockDate()
                    } else {
                        spctime = getDayOfWeekSpcTime() // common_service.getLockTimeUseable()
                    }

                    exe_start_time = System.currentTimeMillis()
                    WriteLog("시간 조회")
                }

                // 패스워드 입력을 성공했으면, 사용시간 초기화
                if (common_service!!.getIsCheckedPwd() && !isLockApp()) {
                    spctime = lmttime
                    exe_start_time = System.currentTimeMillis()
                    common_service?.setIsCheckedPwd(false)

                    common_service?.setLogMsg("${getGubun()}[패스워드 입력 시간 reset] $spctime")
                    common_service?.setLockDate()
                }

                // 사용 가능 시간 = 남은 시간 - (현재시간 - 어플 실행시간)
                usabletime = spctime - (System.currentTimeMillis() - exe_start_time)

                sPref_step = 1

                if (usabletime <= 0) {
                    // 타이머 뷰 중지
                    stopTimerService("STEP 9")

                    unlockedPkgname = packageName!!
                    usabletime = 0
                    common_service?.setLockDate()

                    // 잠금 화면 호출
                    showLockView()
                    sPref_step = 0
                }
            }
        } catch (e: Exception) {
            common_service?.setLogMsg("<< 비정상 종료 >> $e")
        }
    }

    /**
     * 이전 앱이 잠금 앱인지 여부
     */
    private fun isLockApp(): Boolean {
        if (select_list == null || select_list!!.isEmpty()) return false

        select_list?.forEach { item ->
            if (item["pkgnme"].toString() == pre_packageName && item["exe_gbn"].toString() == "2") {
                return true
            }
        }

        return false
    }

    /**
     * 앱별 남은 시간 저장
     */
    private fun setSaveAppSpcTime(value: String) {
        val whereClause = "pkgnme=?"
        val values = HashMap<Any, Any>()
        values["exe_eddte"] = System.currentTimeMillis()
        values["spctime"] = usabletime
        common_service?.setLogMsg("${getGubun()} 남은 시간 저장, ${getCountDown(usabletime)}")
        dbm?.executeUpdate("applist", values, whereClause, arrayOf(value))
    }

    /**
     * 일일 남은 시간 저장
     */
    private fun setSaveDailySpcTime() {
        if (dayWeek == null) return

        val whereClause = "dayweek=?"
        val values = HashMap<Any, Any>()
        values["exe_eddte"] = System.currentTimeMillis()
        values["spctime"] = usabletime
        common_service?.setLogMsg("${getGubun()} 남은 시간 저장, $dayWeek, ${getCountDown(usabletime)}")
        dbm?.executeUpdate("priorlist", values, whereClause, arrayOf(dayWeek!!))

        // 종료 처리된 내용을 업데이트 후 다시 조회해준다.
        prior_list = dbm?.executeSelect("SELECT * FROM priorlist", null)
    }

    /**
     * 종료 처리
     */
    @SuppressLint("SimpleDateFormat")
    @Synchronized
    private fun finishProcess() {
        val screenOn = common_service?.getScreenOn() ?: false
        val curTime = System.currentTimeMillis()

        // 종료 처리
        if (pre_packageName != packageName) {
            try {
                updateFinishInfo()
            } catch (e: Exception) {
                common_service?.setLogMsg("finishProcess : $e")
            }

            // 패스워드 승인 관련 초기화
            // 고장 다이얼로그는 패스워드 승인이 없는 관계로 !sPref_mode.equals("1") 조건을 둔다.
            if (unlockedPkgname != packageName && sPref_mode != "1") {
                common_service?.setIsCheckedPwd(false)
            }
        }

        // auto_save초 마다 자동 저장
        if (screenOn) {
            if (IsTargetApp || isDailyAbleApp) {
                // 일일 시간 제한
                if (sPref_timeMode && !getIsAllowTimeLockApp() && curTime - starttime >= auto_save) {
                    starttime = curTime
                    common_service?.setLogMsg("${getGubun()} 자동 저장 $pre_packageName, $packageName")
                    setSaveDailySpcTime()
                }
                // 앱별 시간 제한
                else if (!sPref_timeMode && "1" == exe_gbn && curTime - starttime >= auto_save) {
                    starttime = curTime
                    common_service?.setLogMsg("${getGubun()} 자동 저장 $pre_packageName, $packageName")
                    setSaveAppSpcTime(packageName!!)
                }
            }
            // 사용시간
            if (curTime - starttime_his >= auto_save) {
                if (SCREEN_ON && STATUS_ICON != "13") {
                    try {
                        mNotification?.setLatestEventInfo(
                            this,
                            getString(R.string.app_name),
                            getTodayUsed(),
                            pendingIndent
                        )
                        nm?.notify(noti_id, mNotification)
                    } catch (e: Exception) {
                        common_service?.setLogMsg("노티 갱신 오류 : $e")
                    }
                }

                setHistory()
                sPref_step_his = 0
            }
        }

        // 현재 실행중인 패키지명 갱신
        pre_packageName = packageName!!
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTodayUsed(): String {
        val selectionArgs = arrayOf(app_stdte ?: formatter.format(Date()))
        val sql = "select sum(exetime) as exetime from appcount where exedte = ? and pkgnme <> '1'"
        val select = dbm?.executeSelect(sql, selectionArgs)

        return if (select != null && select.isNotEmpty() && select[0]["exetime"] != null) {
            "${getString(R.string.txt_today_used)} ${
                getCountDown(
                    select[0]["exetime"].toString().toLong()
                )
            }"
        } else {
            "${getString(R.string.txt_today_used)} ${getCountDown(0)}"
        }
    }

    /**
     * 어플 종료 처리
     */
    @Synchronized
    private fun updateFinishInfo() {
        // 최초 실행인 경우 이전 package명이 없는 관계로 리턴 시킨다.
        if (pre_packageName == "1") {
            return
        }

        // 여러 번 실행 방지
        if (updatedFinishCnt > 0) return
        updatedFinishCnt++

        // 사용시간 및 사용 횟수
        setHistory()
        sPref_step_his = 0

        // 앱별 시간제한이고 현재 어플이 시간제한 어플이 아닌 경우
        // (시간 제한 어플이 아닌 경우)
        if (!sPref_timeMode && !getIsTargetApp(pre_packageName, true)) return

        common_service?.setIsCheckedWiFi(false)

        // 일일 시간제한 종료 처리
        if (sPref_timeMode) {
            setSaveDailySpcTime()
            WriteLog("${getGubun()} 어플 종료")
        }
        // 앱별 시간제한 종료 처리
        else if (!sPref_timeMode && "1" == exe_gbn) {
            setSaveAppSpcTime(pre_packageName)
            WriteLog("${getGubun()} 어플 종료")
        }
        sPref_step = 0
    }

    /**
     * 사용시간 및 사용 횟수 카운트
     */
    private fun setHistory() {
        if (getHome(pre_packageName)) return

        val sql = "select * from appcount where pkgnme = ? and exedte = ?"
        val selectionArgs = arrayOf(pre_packageName, TODAY!!)
        val select = dbm?.executeSelect(sql, selectionArgs)

        val cur_time = System.currentTimeMillis()
        var exe_used_time = cur_time - exe_start_time_his
        exe_used_time = if (exe_used_time < 0) 0 else exe_used_time

        if (exe_used_time == 0) return

        // insert
        if (select == null || select.isEmpty()) {
            val cnt = 1L

            val values = HashMap<Any, Any>()
            values["pkgnme"] = pre_packageName
            // 현재 날짜
            values["exedte"] = TODAY!!
            // 실행 시간 (사용시간)
            values["exetime"] = exe_used_time
            // 실행 횟수
            values["execnt"] = cnt
            dbm?.executeInsert("appcount", values)
        }
        // update
        else {
            var cnt = select[0]["execnt"].toString().toLong()
            if (packageName != pre_packageName) cnt++

            exe_used_time += select[0]["exetime"].toString().toLong()

            val whereClause = "pkgnme=? and exedte=?"
            val values = HashMap<Any, Any>()
            values["exedte"] = TODAY!!
            values["exetime"] = exe_used_time
            values["execnt"] = cnt
            dbm?.executeUpdate("appcount", values, whereClause, arrayOf(pre_packageName, TODAY!!))
        }

        exe_start_time_his = cur_time
    }

    /**
     * 잠금 어플 설정 처리
     */
    private fun setLockApp() {
        // 타이머 뷰 중지
        stopTimerService("STEP 10")

        sPref_step = 0

        if (!isSMSAllLock && showWiFiView()) return

        // 비번 입력 화면 실행 조건
        // 암호를 입력하지 않은 경우 또는 잘못 입력된 경우 (isCheckedPwd == false)
        if (!common_service!!.getIsCheckedPwd()) {
            unlockedPkgname = packageName!!
            showLockView(true, false)
        }
    }

    /**
     * 와이파이 접속 여부
     */
    private fun checkWifi(): Boolean {
        val wifi = cManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return wifi?.isConnected ?: false
    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                10 -> setUI()
                11 -> {
                    if (sPref_option6) {
                        if (bluelightView == null) {
                            initBlueLightView()
                            bluelight_params?.alpha = bluelightValue
                        } else if (bluelight_params?.alpha != bluelightValue) {
                            stopBlueLight("")
                            initBlueLightView()
                            common_service?.setLogMsg("sPref_option6 = $sPref_option6, bluelightValue = ${setting_service?.getBluelight()}, $bluelightValue, bluelight_params.alpha = ${bluelight_params?.alpha}")
                        }
                    } else {
                        stopBlueLight("")
                    }
                }
            }
        }
    }

    /**
     * 블루 라이트 차단 뷰
     */
    @Suppress("DEPRECATION")
    private fun initBlueLightView() {
        try {
            val containerParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            bluelightView = LinearLayout(this).apply {
                // 파란색의 보색(주황색)
                setBackgroundColor(Color.rgb(255, 127, 0))
                layoutParams = containerParams
            }

            bluelight_params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,     // 항상 최 상위에 있게
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // 터치 인식, 나중에 기능 추가를 위해 일단 넣어둠
                PixelFormat.TRANSLUCENT                             // 투명
            )

            bluelight_params?.alpha = bluelightValue
            bluelightWm = getSystemService(WINDOW_SERVICE) as WindowManager // 윈도우 매니저 불러옴
            bluelightWm?.addView(bluelightView, bluelight_params)
        } catch (e: Exception) {
            common_service?.setLogMsg("initBlueLightView : $e")
        }
    }

    /**
     * 타이머 뷰 초기화
     */
    private fun initTimerView() {
        try {
            val containerParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = 10
            }

            timerView = TextView(this).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(Color.BLACK)
                setPaintFlags(paintFlags or Paint.FAKE_BOLD_TEXT_FLAG)
                setShadowLayer(1.5f, 1.5f, 1.5f, 0xFFffffff.toInt())
                layoutParams = containerParams
                gravity = Gravity.LEFT or Gravity.BOTTOM
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,     // 항상 최 상위에 있게
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // 터치 인식, 나중에 기능 추가를 위해 일단 넣어둠
                PixelFormat.TRANSLUCENT                             // 투명
            )

            wm = getSystemService(WINDOW_SERVICE) as WindowManager // 윈도우 매니저 불러옴
            wm?.addView(timerView, params)
        } catch (e: Exception) {
            common_service?.setLogMsg("initTimerView : $e")
        }
    }

    /**
     * 타이머 시작
     */
    private fun startTimerService(msg: String) {
        START_TIMER = true
    }

    private fun setUI() {
        if (START_TIMER && sPref_option1 && usabletime > 0) {
            if (timerView == null) {
                initTimerView()
            } else {
                val time = getCountDown(usabletime)
                timerView?.visibility = 0
                timerView?.text = " $time"
            }
        }
        if (usabletime <= 0) {
            stopTimerService("")
        }
    }

    /**
     * 블루라이트 종료
     */
    @SuppressLint("HandlerLeak")
    private fun stopBlueLight(msg: String) {
        if (bluelightWm != null) {
            if (bluelightView != null) bluelightWm?.removeView(bluelightView)
            bluelightView = null
        } else {
            if (bluelightView != null) (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(
                bluelightView
            )
        }
    }

    /**
     * 타이머 종료
     */
    @SuppressLint("HandlerLeak")
    private fun stopTimerService(msg: String) {
        START_TIMER = false

        if (wm != null) {
            if (timerView != null) wm?.removeView(timerView)
            timerView = null
        } else {
            if (timerView != null) (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(
                timerView
            )
        }
    }

    private fun getGubun(): String {
        return if (sPref_timeMode) "[일일]" else "[앱별]"
    }

    /**
     * 시간 제한 어플인 경우
     */
    @Synchronized
    private fun getIsTargetApp(pkgnme: String): Boolean {
        return getIsTargetApp(pkgnme, false)
    }

    /**
     * 시간 제한 어플인 경우
     */
    @Synchronized
    private fun getIsTargetApp(pkgnme: String, isPrePackage: Boolean): Boolean {
        // sms 수신인 경우 모드 잠금
        if (isSMSAllLock) return true

        if (select_list == null) return false

        select_list?.forEachIndexed { index, item ->
            val tmp = item["pkgnme"].toString()
            if (pkgnme == tmp) {
                if (!isPrePackage) {
                    targetIndex = index
                }
                return true
            }
        }

        return false
    }

    /**
     * 잠금 어플 여부
     */
    private fun getIsTargetLockApp(): Boolean {
        // sms 수신인 경우 모두 잠그기 위해
        if (isSMSAllLock) return true

        if (select_list == null) return false

        select_list?.forEachIndexed { index, item ->
            val tmp = item["pkgnme"].toString()
            val tmpExegbn = item["exe_gbn"].toString()
            if (packageName == tmp && "2" == tmpExegbn) {
                targetIndex = index
                return true
            }
        }

        return false
    }

    /**
     * 일일 잠금 설정의 허용 가능 어플 여부
     * @return true : 허용 가능 어플, false : 시간 제한 어플
     */
    private fun getIsAllowTimeLockApp(): Boolean {
        // sms 수신인 경우 모두 잠그기 위해
        if (isSMSAllLock) return true

        if (allow_list == null || allow_list!!.isEmpty()) return false

        allow_list?.forEach { item ->
            val tmp = item["pkgnme"].toString()
            if (packageName == tmp) return true
        }

        return false
    }

    /**
     * 일일 잠금 설정 사용시간
     */
    private fun getDayOfWeekSetting() {
        prior_list?.forEach { item ->
            val tmp = isAbleToday(item["dayweek"].toString())
            if (tmp) {
                // 설정된 사용 시간
                lmttime = item["lmttime"].toString().toLong()
                // 설정된 요일
                dayWeek = item["dayweek"].toString()

                common_service?.setLogMsg(
                    "[일일][시간 설정 조회], $app_stdte, $dayWeek, lmttime=${
                        getCountDown(
                            lmttime
                        )
                    }"
                )
                return
            }
        }
    }

    /**
     * 일일 남은 시간 조회
     */
    private fun getDayOfWeekSpcTime(): Long {
        var rtn = 0L

        prior_list?.forEach { item ->
            val tmp = isAbleToday(item["dayweek"].toString())
            if (tmp) {
                rtn = item["spctime"].toString().toLong()
                return@forEach
            }
        }

        return rtn
    }

    /**
     * 오늘이 일일 사용시간 제한 설정에 해당하는지 여부
     */
    private fun getIsDayOfWeek(): Boolean {
        // sms 수신인 경우 모두 잠그기 위해
        if (isSMSAllLock) return true

        if (prior_list == null) return false

        prior_list?.forEach { item ->
            val tmp = isAbleToday(item["dayweek"].toString())
            if (tmp) return true
        }

        return false
    }

    /**
     * 오늘이 앱별 사용시간 제한 설정에 해당하는지 여부
     */
    private fun getIsDayOfWeekApp(): Boolean {
        return isAbleToday(dayWeekApp)
    }

    /**
     * 일일 잠금 설정에서 금일이 사용 가능 일인지 리턴
     */
    private fun isAbleToday(dayofweek: String?): Boolean {
        if (dayofweek == null) return false

        val oCalendar = Calendar.getInstance()
        val today = oCalendar.get(Calendar.DAY_OF_WEEK)
        val dayofweeks = dayofweek.split("|")

        return dayofweeks[today + (today - 1)] == "1"
    }

    /**
     * WiFi 접속 확인 화면
     */
    private fun showWiFiView(): Boolean {
        if (!isOnWiFi) return false

        // 와이파이 접속시 실행인 경우
        if (!common_service!!.getIsCheckedWiFi() && !checkWifi()) {
            val i = Intent(this, SkyLockViewWiFi::class.java).apply {
                putExtra("PKGNME", packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            startActivity(i)

            return true
        }

        return false
    }

    /**
     * 잠금 처리
     */
    private fun showLockView() {
        showLockView(false, false)
    }

    /**
     * 잠금 처리
     * @param isLockApp : 락이 걸린 어플일 경우 true
     * @param isDeletedApp : 삭제 화면일 경우 true
     */
    @SuppressLint("InlinedApi")
    private synchronized
    fun showLockView(isLockApp: Boolean, isDeletedApp: Boolean) {
        synchronized(this) {
            when (lock_mode) {
                "1" -> {
                    val i = Intent().apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(i)

                    val dialogIntent = Intent(this, SkyDialogActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        putExtra("PKGNAME", packageName)
                    }
                    startActivity(dialogIntent)
                }

                "2" -> {
                    // 비번
                    val i = Intent(this, SkyLockView::class.java).apply {
                        putExtra("MODE", "ACT") // 실행 버전 : ACT, 비밀번호 변경설정 버전 : PWD
                        putExtra("ISLOCK", isLockApp)
                        putExtra("ISDELETED", isDeletedApp)
                        putExtra("LMTTIME", lmttime)
                        putExtra("PKGNME", packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(i)
                }

                "3" -> {
                    // 패턴
                    val i = Intent(this, SkyLockPatternView::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        putExtra(SkyLockPatternView._Mode, SkyLockPatternView.LPMode.ComparePattern)
                        putExtra(SkyLockPatternView._AutoSave, true)
                        putExtra("MODE", "ACT") // 실행 버전 : ACT, 비밀번호 변경설정 버전 : PWD
                        putExtra("ISLOCK", isLockApp)
                        putExtra("ISDELETED", isDeletedApp)
                        putExtra("LMTTIME", lmttime)
                        putExtra("PKGNME", packageName)
                    }
                    startActivity(i)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        if (on_off_Receiver != null) {
            try {
                unregisterReceiver(on_off_Receiver)
            } catch (e: IllegalArgumentException) {
                common_service?.setLogMsg("${getGubun()}onDestroy error : IllegalArgumentException")
            }
        }

        common_service?.setLogMsg("@@@ Die @@@")

        // 쓰레드 제거
        destoryThread()

        common_service?.setIsInit(true)
        common_service?.setRefreshSetting(true)
        startService(Intent(ACTION))

        super.onDestroy()

        // 카운터 뷰 제거
        stopTimerService("onDestroy")
        stopBlueLight("onDestroy")
    }

    private fun getCountDown(USETIME: Long): String {
        return "${getHH(USETIME)}:${getMM(USETIME)}:${getmm(USETIME)}"
    }

    private fun getHH(USETIME: Long): String {
        val time = (USETIME / (1000 * 60 * 60)) % 24
        return if (time < 10) "0$time" else "$time"
    }

    private fun getMM(USETIME: Long): String {
        val time = (USETIME / (1000 * 60)) % 60
        return if (time < 10) "0$time" else "$time"
    }

    private fun getmm(USETIME: Long): String {
        val time = (USETIME / 1000) % 60
        return if (time < 10) "0$time" else "$time"
    }

    private fun WriteLog(msg: String) {
        common_service?.setLogMsg(
            "${getGubun()}$targetIndex[$msg] $pre_packageName, $packageName[${
                getCountDown(
                    usabletime
                )
            }], app_stdte=$app_stdte, $TODAY"
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}