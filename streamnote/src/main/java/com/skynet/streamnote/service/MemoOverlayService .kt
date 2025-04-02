package com.skynet.streamnote.service

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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
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
    private val handler = Handler(Looper.getMainLooper())
    private var scrollAnimator: ValueAnimator? = null

    companion object {
        const val ACTION_UPDATE_THEME = "com.skynet.streamnote.ACTION_UPDATE_THEME"
        const val EXTRA_THEME_ID = "theme_id"
        const val NOTIFICATION_ID = 1001
        private const val SCROLL_DURATION = 10000L // 스크롤 지속 시간
        private const val PAUSE_DURATION = 3000L // 일시 정지 시간
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 활성화된 메모 가져오기
        CoroutineScope(Dispatchers.Main).launch {
            try {
                memoRepository.getActiveMemos().collect { memos ->
                    if (memos.isEmpty()) {
                        updateNoMemoView()
                    } else {
                        // 메모 생성 시간 순으로 정렬
                        currentMemos = memos.sortedByDescending { it.createdAt }

                        // 첫 번째 메모부터 시작
                        currentMemoIndex = 0

                        // 초기 메모 표시
                        updateOverlayContent()
                    }
                }
            } catch (e: Exception) {
                Log.e("MemoOverlayService", "Error fetching memos", e)
                updateNoMemoView()
            }
        }

        // 전역 테마 설정 로드
        val sharedPrefs = getSharedPreferences("streamnote_preferences", Context.MODE_PRIVATE)
        val globalThemeId = sharedPrefs.getInt("global_theme_id", 1)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val theme = themeRepository.getThemeById(globalThemeId).first()
                currentTheme = theme ?: themeRepository.getAllThemes().first().firstOrNull()
                updateOverlayAppearance()
            } catch (e: Exception) {
                Log.e("MemoOverlayService", "Error loading theme", e)
            }
        }

        createOverlayView()
    }

    private fun updateNoMemoView() {
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)
        textView?.text = getString(R.string.no_memo_to_display)

        // 애니메이션 중지
        stopScrolling()
    }

    private fun stopScrolling() {
        scrollAnimator?.cancel()
        handler.removeCallbacksAndMessages(null)

        // 텍스트 뷰 위치 초기화
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)
        textView?.translationX = 0f
    }

    private fun updateOverlayContent() {
        if (currentMemos.isEmpty()) {
            updateNoMemoView()
            return
        }

        // 기존 애니메이션 중지
        stopScrolling()

        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)

        // 현재 인덱스의 메모 표시
        textView?.apply {
            // 최대 10줄까지 표시
            maxLines = 10
            text = currentMemos[currentMemoIndex].content
        }

        // 다음 메모 인덱스 준비
        val nextIndex = (currentMemoIndex + 1) % currentMemos.size

        // 텍스트 스크롤 시작
        startScrollingAnimation {
            // 애니메이션 완료 후 다음 메모로 이동
            currentMemoIndex = nextIndex
            updateOverlayContent()
        }
    }

    private fun startScrollingAnimation(onAnimationComplete: (() -> Unit)? = null) {
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView) ?: return

        textView.post {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val textWidth = textView.width

            // 텍스트가 화면보다 짧으면 스크롤하지 않음
            if (textWidth <= screenWidth) {
                // 잠시 후 다음 메모로 넘어감
                handler.postDelayed({
                    onAnimationComplete?.invoke()
                }, PAUSE_DURATION)
                return@post
            }

            // 스크롤 애니메이터 생성
            scrollAnimator = ValueAnimator.ofFloat(0f, (-(textWidth + screenWidth)).toFloat()).apply {
                // 테마의 스크롤 속도 반영
                val scrollSpeed = currentTheme?.scrollSpeed ?: 1f
                duration = (SCROLL_DURATION / scrollSpeed).toLong()
                interpolator = LinearInterpolator()

                addUpdateListener { animator ->
                    val value = animator.animatedValue as Float
                    textView.translationX = value
                }

                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationStart(animation: android.animation.Animator) {}

                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // 애니메이션 종료 후 잠시 대기
                        handler.postDelayed({
                            // 텍스트 위치 초기화
                            textView.translationX = 0f
                            // 다음 메모로 전환
                            onAnimationComplete?.invoke()
                        }, PAUSE_DURATION)
                    }

                    override fun onAnimationCancel(animation: android.animation.Animator) {}
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })

                start()
            }
        }
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

    private fun createOverlayView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.memo_overlay, null)

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

        // 텍스트뷰 설정 변경
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)
        textView?.apply {
            // 단일 라인 제한 해제
            maxLines = Integer.MAX_VALUE
            ellipsize = null
        }

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
    }

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
                            try {
                                themeRepository.getThemeById(themeId).collect { theme ->
                                    if (theme != null) {
                                        currentTheme = theme
                                        updateOverlayAppearance()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MemoOverlayService", "Error updating theme", e)
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
            // 모든 핸들러 메시지와 콜백 제거
            handler.removeCallbacksAndMessages(null)

            // 애니메이션 중지
            scrollAnimator?.cancel()

            // 오버레이 뷰 제거
            if (windowManager != null && overlayView != null) {
                try {
                    windowManager?.removeView(overlayView)
                } catch (e: Exception) {
                    Log.e("MemoOverlayService", "Error removing overlay view", e)
                }
                overlayView = null
            }
        }
    }