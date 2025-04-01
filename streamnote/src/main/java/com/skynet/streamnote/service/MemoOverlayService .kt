package com.skynet.streamnote.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.skynet.streamnote.R
import com.skynet.streamnote.data.AppDatabase
import com.skynet.streamnote.data.entity.Memo
import com.skynet.streamnote.data.entity.Theme
import com.skynet.streamnote.data.repository.MemoRepository
import com.skynet.streamnote.data.repository.ThemeRepository
import com.skynet.streamnote.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MemoOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val memoRepository by lazy { MemoRepository(AppDatabase.getDatabase(this).memoDao()) }
    private val themeRepository by lazy { ThemeRepository(AppDatabase.getDatabase(this).themeDao()) }
    private var currentMemos: List<Memo> = emptyList()
    private var currentMemoIndex = 0
    private var currentTheme: Theme? = null
    private var animation: ObjectAnimator? = null

    companion object {
        const val ACTION_UPDATE_THEME = "com.skynet.streamnote.ACTION_UPDATE_THEME"
        const val EXTRA_THEME_ID = "theme_id"
        const val NOTIFICATION_ID = 1001
    }

    // 서비스가 시작될 때 전역 테마 ID 로드
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 활성화된 메모 가져오기
        CoroutineScope(Dispatchers.Main).launch {
            memoRepository.getActiveMemos().collect { memos ->
                currentMemos = memos
                updateOverlayContent()
            }
        }

        // 전역 테마 설정 로드
        val sharedPrefs = getSharedPreferences("streamnote_preferences", Context.MODE_PRIVATE)
        val globalThemeId = sharedPrefs.getInt("global_theme_id", 1) // 기본값 1 (첫 번째 테마)

        CoroutineScope(Dispatchers.Main).launch {
            val theme = themeRepository.getThemeById(globalThemeId).first()
            currentTheme = theme ?: themeRepository.getAllThemes().first().firstOrNull()
            updateOverlayAppearance()
        }

        createOverlayView()
    }

    private fun createOverlayView() {
        // Inflate overlay layout
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.memo_overlay, null)

        // 오버레이 레이아웃 파라미터 설정
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // 현재 테마의 위치 설정 적용
        params.gravity = if (currentTheme?.position == "TOP") Gravity.TOP else Gravity.BOTTOM

        // 여백 설정 적용
        val marginTop = currentTheme?.marginTop ?: 0
        val marginBottom = currentTheme?.marginBottom ?: 0
        val marginHorizontal = currentTheme?.marginHorizontal ?: 0

        // dp를 픽셀로 변환
        val density = resources.displayMetrics.density
        params.y = if (params.gravity == Gravity.TOP)
            (marginTop * density).toInt()
        else
            (marginBottom * density).toInt()

        params.x = (marginHorizontal * density).toInt()

        // 창 관리자에 뷰 추가
        windowManager?.addView(overlayView, params)

        // 오버레이 컨테이너 스타일 적용
        val container = overlayView?.findViewById<LinearLayout>(R.id.overlayContainer)

        // 배경색 적용 (테마에서 가져오거나 기본값 사용)
        val backgroundColor = currentTheme?.backgroundColor ?:
        ContextCompat.getColor(this, R.color.primary) and 0x00FFFFFF or 0xC0000000.toInt()

        container?.setBackgroundColor(backgroundColor)
        container?.setPadding(
            (marginHorizontal * density).toInt(),
            0,
            (marginHorizontal * density).toInt(),
            0
        )

        startScrollingAnimation()
    }

    private fun updateOverlayContent() {
        if (currentMemos.isEmpty()) {
            overlayView?.findViewById<TextView>(R.id.overlayTextView)?.text = getString(R.string.no_memo_to_display)
            return
        }

        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)
        textView?.text = currentMemos[currentMemoIndex].content

        // 다음 메모로 순환
        currentMemoIndex = (currentMemoIndex + 1) % currentMemos.size
    }

    private fun updateOverlayAppearance() {
        val theme = currentTheme ?: return
        val container = overlayView?.findViewById<View>(R.id.overlayContainer)
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)

        // 테마 적용
        container?.setBackgroundColor(theme.backgroundColor)
        textView?.setTextColor(theme.textColor)
        textView?.textSize = theme.textSize

        // 폰트 설정 적용
        val typeface = when (theme.fontFamily) {
            "Serif" -> Typeface.SERIF
            "Monospace" -> Typeface.MONOSPACE
            "SansSerif" -> Typeface.SANS_SERIF
            else -> Typeface.DEFAULT
        }

        val style = when {
            theme.isBold && theme.isItalic -> Typeface.BOLD_ITALIC
            theme.isBold -> Typeface.BOLD
            theme.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        textView?.typeface = Typeface.create(typeface, style)

        // 위치 및 여백 적용
        updateOverlayPosition(theme)

        // 테마 변경 시 애니메이션 다시 시작
        startScrollingAnimation()
    }

    private fun updateOverlayPosition(theme: Theme) {
        val params = overlayView?.layoutParams as? WindowManager.LayoutParams ?: return

        // 위치 설정
        params.gravity = if (theme.position == "TOP") Gravity.TOP else Gravity.BOTTOM

        // 여백 설정
        val density = resources.displayMetrics.density
        params.y = if (params.gravity == Gravity.TOP)
            (theme.marginTop * density).toInt()
        else
            (theme.marginBottom * density).toInt()

        params.x = (theme.marginHorizontal * density).toInt()

        // 레이아웃 파라미터 업데이트
        windowManager?.updateViewLayout(overlayView, params)

        // 컨테이너 패딩 설정
        val container = overlayView?.findViewById<LinearLayout>(R.id.overlayContainer)
        container?.setPadding(
            (theme.marginHorizontal * density).toInt(),
            0,
            (theme.marginHorizontal * density).toInt(),
            0
        )
    }

    private fun startScrollingAnimation() {
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView) ?: return

        // 기존 애니메이션 중지
        animation?.cancel()

        // 화면 너비 계산
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // 애니메이션 속도 계산 (스크롤 속도가 높을수록 애니메이션 시간은 짧아져야 함)
        val scrollSpeed = currentTheme?.scrollSpeed ?: 1f
        val duration = (10000 / scrollSpeed).toLong()

        // 텍스트 뷰의 너비 측정을 위해 처리
        textView.post {
            val textWidth = textView.width.toFloat()

            // 애니메이션 설정 - 화면 오른쪽에서 왼쪽으로 이동
            animation = ObjectAnimator.ofFloat(textView, "translationX", screenWidth.toFloat(), -textWidth).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                // 무한반복 대신 한 번만 실행
                repeatCount = 0

                // 애니메이션이 끝나면 다음 메모로 넘어감
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        // 다음 메모로 넘어감
                        updateOverlayContent()
                        // 새 메모로 애니메이션 다시 시작
                        startScrollingAnimation()
                    }
                })

                start()
            }
        }
    }

    // 알림 채널 생성
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "overlay_service_channel",
                "StreamNote Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "StreamNote 오버레이 서비스"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 포그라운드 서비스 시작
    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else
                0
        )

        val primaryColor = ContextCompat.getColor(this, R.color.primary)

        val notification = NotificationCompat.Builder(this, "overlay_service_channel")
            .setContentTitle(getString(R.string.service_running))
            .setContentText(getString(R.string.service_description))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setColor(primaryColor)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_UPDATE_THEME -> {
                    val themeId = it.getIntExtra(EXTRA_THEME_ID, 0)
                    if (themeId > 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            themeRepository.getThemeById(themeId).collect { theme ->
                                if (theme != null) {
                                    currentTheme = theme
                                    updateOverlayAppearance()
                                    // 테마 업데이트 시 애니메이션도 다시 시작
                                    startScrollingAnimation()
                                }
                            }
                        }
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        animation?.cancel()
        if (windowManager != null && overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }
}