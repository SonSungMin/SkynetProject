/*
 *   Copyright 2012 Hai Bison
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.skynet.skytimelock.view

import group.pals.android.lib.ui.lockpattern.util.IEncrypter
import group.pals.android.lib.ui.lockpattern.util.InvalidEncrypterException
import group.pals.android.lib.ui.lockpattern.widget.LockPatternUtils
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.Cell
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.DisplayMode

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.skynet.common.SkyTimeLockSetting
import com.skynet.db.DbManager
import com.skynet.framework.SkyNetBaseActivity
import com.skynet.skytimelock.free.R

/**
 * Main activity for this library.
 *
 * @author Hai Bison
 * @since v1.0
 */
class SkyLockPatternView : SkyNetBaseActivity() {
    /**
     * Mode for [SkyLockPatternView]. Default is
     * [LPMode.CreatePattern]<br>
     * Acceptable values:<br>
     * - [LPMode.CreatePattern]<br>
     * - [LPMode.ComparePattern]
     */
    companion object {
        val _Mode = LPMode::class.java.name

        val _ClassName = SkyLockPatternView::class.java.name

        /**
         * Specify if the pattern will be saved automatically or not. Default =
         * `false`
         */
        val _AutoSave = "$_ClassName.auto_save"

        /**
         * Maximum retry times, in mode [ComparePattern], default is
         * `5`.
         */
        val _MaxRetry = "$_ClassName.max_retry"

        /**
         * Key to hold pattern. Can be a SHA-1 string <i><b>or</b></i> an encrypted
         * string of its (if [_EncrypterClass] is used).
         *
         * @since v2 beta
         */
        val _Pattern = "$_ClassName.pattern"

        /**
         * Key to hold implemented class of [IEncrypter].<br>
         * If `null`, nothing will be used.
         *
         * @since v2 beta
         */
        val _EncrypterClass = IEncrypter::class.java.name
    }

    /**
     * Lock pattern mode for this activity.
     *
     * @author Hai Bison
     * @since v1.3 alpha
     */
    enum class LPMode {
        /**
         * Creates new pattern.
         */
        CreatePattern,
        /**
         * Compares to existing pattern.
         */
        ComparePattern
    }

    private lateinit var mMode: LPMode
    private var mMaxRetry: Int = 0
    private var mAutoSave: Boolean = false
    private var mEncrypter: IEncrypter? = null

    private lateinit var mTxtInfo: TextView
    private lateinit var mLockPatternView: LockPatternView
    private lateinit var mFooter: View
    private lateinit var mBtnCancel: Button
    private lateinit var mBtnConfirm: Button

    private lateinit var lay_keypad: LinearLayout

    private var MODE: String? = null
    private var isLockView: Boolean = false
    private var isDeleted: Boolean = false
    private var pkgnme: String? = null
    private var sPref_pwd: String? = null
    private val defaultPwd: String = "777" // 초기 암호
    private var orgPwd: String? = null
    private var chgPwd: String? = null
    private var LMTTIME: Long = 0

    private lateinit var mPrefs: SharedPreferences
    private var sPref_bg: String? = null
    private lateinit var layout_main: LinearLayout
    private lateinit var dbm: DbManager
    private lateinit var setting: SkyTimeLockSetting
    private var sPref_option3: Boolean = false
    private lateinit var lock_app_icon: ImageView

    /** Called when the activity is first created. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setAdActivity(true)
        super.setLayout_Id(R.layout.lock_pattern)

        super.onCreate(savedInstanceState)

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mPrefs = getSharedPreferences(SkyLockPatternView::class.java.name, 0)

        mMode = intent.getSerializableExtra(_Mode) as? LPMode ?: LPMode.CreatePattern

        mMaxRetry = intent.getIntExtra(_MaxRetry, 50)

        // set this to false by default, for security enhancement
        mAutoSave = intent.getBooleanExtra(_AutoSave, false)

        // if false, clear previous values (currently it is the pattern only)
        if (!mAutoSave)
            mPrefs.edit().clear().commit()

        // encrypter
        val encrypterClass = intent.getSerializableExtra(_EncrypterClass) as? Class<*>
        if (encrypterClass != null) {
            try {
                mEncrypter = encrypterClass.newInstance() as IEncrypter
            } catch (t: Throwable) {
                throw InvalidEncrypterException()
            }
        }

        init()
    }// onCreate()

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * 백버튼이 눌렸을때
     */
    override fun onBackPressed() {
        if("ACT" == MODE || isLockView) {
            val i = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            startActivity(i)
        } else {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        init()
        super.onConfigurationChanged(newConfig)
    }// onConfigurationChanged()

    private fun init() {
        // in case screen orientation changed, save all controls' state to
        // restore later
        val info = if (::mTxtInfo.isInitialized) mTxtInfo.text else null
        val btnConfirmText = if (::mBtnConfirm.isInitialized) mBtnConfirm.text else null
        val btnConfirmEnabled = if (::mBtnConfirm.isInitialized) mBtnConfirm.isEnabled else null
        val lastDisplayMode = if (::mLockPatternView.isInitialized) mLockPatternView.displayMode else null
        val lastPattern = if (::mLockPatternView.isInitialized) mLockPatternView.pattern else null

        setting = common.getSetting("SkyLockPatternView.init")
        dbm = DbManager(this)

        MODE = intent.getStringExtra("MODE")
        sPref_bg = setting.getBg()
        sPref_option3 = setting.isOption3()

        common.setIsCheckedPwd(false)

        isLockView = intent.getBooleanExtra("ISLOCK", false)
        isDeleted = intent.getBooleanExtra("ISDELETED", false)
        pkgnme = intent.getStringExtra("PKGNME")
        LMTTIME = intent.getLongExtra("LMTTIME", 0)

        // 잠금 어플 아이콘
        lock_app_icon = findViewById(R.id.lock_app_icon)
        try {
            val dw = packageManager.getApplicationIcon(pkgnme!!)
            lock_app_icon.setImageDrawable(dw)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        // 배경 설정
        layout_main = findViewById(R.id.layout_pattern)
        layout_main.background = common.getBackgroundImg()

        lay_keypad = findViewById(R.id.lay_keypad)

        mTxtInfo = findViewById(R.id.alp_lpa_text_info)
        mTxtInfo.setTextColor(Color.BLUE)

        mLockPatternView = findViewById(R.id.alp_lpa_lockPattern)

        mFooter = findViewById(R.id.alp_lpa_layout_footer)
        mBtnCancel = findViewById(R.id.alp_lpa_button_cancel)
        mBtnConfirm = findViewById(R.id.alp_lpa_button_confirm)

        // LOCK PATTERN VIEW

        // haptic feedback
        var hapticFeedbackEnabled = false
        try {
            hapticFeedbackEnabled = Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0
        } catch (t: Throwable) {
            // ignore it
        }
        mLockPatternView.setTactileFeedbackEnabled(hapticFeedbackEnabled)

        mLockPatternView.setOnPatternListener(mPatternViewListener)
        if (lastPattern != null && lastDisplayMode != null)
            mLockPatternView.setPattern(lastDisplayMode, lastPattern)

        // COMMAND BUTTONS

        when (mMode) {
            LPMode.CreatePattern -> {
                mBtnCancel.setOnClickListener(mBtnCancelOnClickListener)
                mBtnConfirm.setOnClickListener(mBtnConfirmOnClickListener)

                mFooter.visibility = View.VISIBLE

                if (info != null)
                    mTxtInfo.text = info
                else
                    mTxtInfo.setText(R.string.alp_msg_draw_an_unlock_pattern)

                if (btnConfirmText != null) {
                    mBtnConfirm.text = btnConfirmText
                    btnConfirmEnabled?.let { mBtnConfirm.isEnabled = it }
                }
            }

            LPMode.ComparePattern -> {
                mFooter.visibility = View.GONE

                when (MODE) {
                    "ACT" -> {
                        if (setting.isOption10()) {
                            lay_keypad.visibility = View.VISIBLE
                            isEmptyPattern()
                        } else {
                            lay_keypad.visibility = View.GONE
                        }

                        if (isLockView)
                            mTxtInfo.setText(R.string.app_pattern)
                        else
                            mTxtInfo.setText(R.string.app_timeout)
                    }
                    "START" -> {
                        isEmptyPattern()
                        mTxtInfo.setText(R.string.app_pattern)
                    }
                    else -> {
                        if (info != null)
                            mTxtInfo.text = info
                        else
                            mTxtInfo.setText(R.string.alp_msg_draw_pattern_to_unlock)
                    }
                }
            }
        }

        setResult(RESULT_CANCELED)
    }// init()

    /**
     * 입력된 패턴이 없을 경우 패턴을 설정하는 화면을 불러온다.
     */
    private fun isEmptyPattern() {
        if (setting.getPattern() == null || setting.getPattern() == "") {
            Toast.makeText(this, getText(R.string.err_pattern), Toast.LENGTH_LONG).show()

            val i = Intent(this, SkyLockView::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("MODE", "START") // 처음 실행 : START, 실행 버전 : ACT, 암호설정 버전 : PWD
                putExtra("ISLOCK", false)
                putExtra("ISDELETED", false)
                putExtra("LMTTIME", 0)
                putExtra("PKGNME", "com.skynet.skytimelock.view.SkyTimeLockMainView")
            }
            startActivity(i)

            finish()
        }
    }

    /**
     * Encodes `pattern` to a string.<br>
     *
     * <li>If [_EncrypterClass] is not set, return SHA-1 of
     * `pattern`.</li>
     *
     * <li>If [_EncrypterClass] is set, calculate SHA-1 of
     * `pattern`, then encrypt the SHA-1 value and return the result.</li>
     *
     * @param pattern
     * @return SHA-1 of `pattern`, or encrypted string of its.
     * @since v2 beta
     */
    private fun encodePattern(pattern: List<Cell>): String {
        return if (mEncrypter == null) {
            LockPatternUtils.patternToSha1(pattern)
        } else {
            try {
                mEncrypter!!.encrypt(this, LockPatternUtils.patternToSha1(pattern))
            } catch (t: Throwable) {
                throw InvalidEncrypterException()
            }
        }
    }// encodePattern()

    private var mRetryCount = 0
    private var mLastPattern: MutableList<Cell>? = null

    /**
     * 패턴 검증
     *
     * @param pattern
     */
    private fun doComparePattern(pattern: List<Cell>?) {
        if (pattern == null)
            return

        mLastPattern = ArrayList()
        mLastPattern!!.addAll(pattern)

        var currentPattern = intent.getStringExtra(_Pattern)
        if (currentPattern == null)
            currentPattern = mPrefs.getString(_Pattern, setting.getPattern())

        if (encodePattern(pattern) == currentPattern) {
            if ("ACT" == MODE) {
                common.setIsCheckedPwd(true)
                // sms 잠금 비활성화
                common.setAllLock(false)

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
            } else if ("START" == MODE) {
                val intent = Intent(this, SkyTimeLockMainView::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }

            setResult(RESULT_OK)
            finish()
        } else {
            mRetryCount++

            // 최대 실패 개수를 넘으면 종료
            if (mRetryCount >= mMaxRetry) {
                setResult(RESULT_CANCELED)
                finish()
            } else {
                mTxtInfo.setTextColor(Color.RED)
                mLockPatternView.setDisplayMode(DisplayMode.Wrong)
                mTxtInfo.setText(R.string.alp_msg_try_again)
            }
        }
    }// doComparePattern()

    /**
     * 새로운 패턴 생성
     *
     * @param pattern
     */
    private fun doCreatePattern(pattern: List<Cell>) {
        // 최소 패턴 통과 갯수
        if (pattern.size < 4) {
            mLockPatternView.setDisplayMode(DisplayMode.Wrong)
            mTxtInfo.setText(R.string.alp_msg_connect_4dots)
            return
        }

        common.setLogMsg("mLastPattern = $mLastPattern")

        if (mLastPattern == null) {
            mLastPattern = ArrayList()
            mLastPattern!!.addAll(pattern)
            mTxtInfo.setText(R.string.alp_msg_pattern_recorded)
            mBtnConfirm.isEnabled = true
        } else {
            if (encodePattern(mLastPattern!!) == encodePattern(pattern)) {
                mTxtInfo.setText(R.string.alp_msg_your_new_unlock_pattern)
                mBtnConfirm.isEnabled = true
            } else {
                mTxtInfo.setText(R.string.alp_msg_redraw_pattern_to_confirm)
                mBtnConfirm.isEnabled = false
                mLockPatternView.setDisplayMode(DisplayMode.Wrong)
            }
        }
    }// doCreatePattern()

    private val mPatternViewListener = object : LockPatternView.OnPatternListener {
        override fun onPatternStart() {
            mLockPatternView.setDisplayMode(DisplayMode.Correct)

            if (mMode == LPMode.CreatePattern) {
                mTxtInfo.setText(R.string.alp_msg_release_finger_when_done)
                mBtnConfirm.isEnabled = false
                if (getString(R.string.alp_cmd_continue) == mBtnConfirm.text)
                    mLastPattern = null
            }
        }// onPatternStart()

        override fun onPatternDetected(pattern: List<Cell>) {
            when (mMode) {
                LPMode.CreatePattern -> doCreatePattern(pattern)
                LPMode.ComparePattern -> doComparePattern(pattern)
            }
        }// onPatternDetected()

        override fun onPatternCleared() {
            mLockPatternView.setDisplayMode(DisplayMode.Correct)

            when (mMode) {
                LPMode.CreatePattern -> {
                    mBtnConfirm.isEnabled = false
                    if (getString(R.string.alp_cmd_continue) == mBtnConfirm.text) {
                        mLastPattern = null
                        mTxtInfo.setText(R.string.alp_msg_draw_an_unlock_pattern)
                    } else
                        mTxtInfo.setText(R.string.alp_msg_redraw_pattern_to_confirm)
                }
                LPMode.ComparePattern -> {
                    mTxtInfo.setText(R.string.alp_msg_draw_pattern_to_unlock)
                }
            }
        }// onPatternCleared()

        override fun onPatternCellAdded(pattern: List<Cell>) {
            // TODO Auto-generated method stub
        }
    }// mPatternViewListener

    private val mBtnCancelOnClickListener = View.OnClickListener {
        setResult(RESULT_CANCELED)
        finish()
    }// mBtnCancelOnClickListener

    private val mBtnConfirmOnClickListener = View.OnClickListener {
        var ptn = ""
        if (getString(R.string.alp_cmd_continue) == mBtnConfirm.text) {
            mLockPatternView.clearPattern()
            mTxtInfo.setText(R.string.alp_msg_redraw_pattern_to_confirm)
            mBtnConfirm.setText(R.string.alp_cmd_confirm)
            mBtnConfirm.isEnabled = false
        } else {
            mPrefs.edit().putString(_Pattern, encodePattern(mLastPattern!!)).commit()

            ptn = encodePattern(mLastPattern!!)
            setting.setPattern(ptn)

            common.setLogMsg("mLastPattern = $mLastPattern, ptn=$ptn, setting.getPattern()=${setting.getPattern()}")

            common.setSetting(setting)

            val i = Intent()
            i.putExtra(_Pattern, ptn)
            setResult(RESULT_OK, i)
            finish()
        }
    }// mBtnConfirmOnClickListener
}