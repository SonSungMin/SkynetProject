package com.skynet.common

/**
 * 시간 잠금 설정 클래스
 */
class SkyTimeLockSetting {
    /**
     * 잠금 화면 종류(번호, 패턴)
     * 잠금 화면 (1:고장화면, 2:비번, 3:패턴)
     */
    private var _mode: String? = null
    fun getMode(): String? = _mode
    fun setMode(mode: String?) {
        this._mode = mode
    }

    /**
     * 잠금 방법 (앱별 잠금, 일일 사용시간 제한)
     * 잠금 방법 (1:앱별 잠금, 2:일일잠금)
     */
    private var _lock_mode: String? = null
    fun getLock_mode(): String? = _lock_mode
    fun setLock_mode(lock_mode: String?) {
        this._lock_mode = lock_mode
    }

    private var _bg: String? = null
    fun getBg(): String? = _bg
    fun setBg(bg: String?) {
        this._bg = bg
    }

    private var _pwd: String? = null
    fun getPwd(): String? = _pwd
    fun setPwd(pwd: String?) {
        this._pwd = pwd
    }

    private var _pattern: String? = null
    fun getPattern(): String? = _pattern
    fun setPattern(pattern: String?) {
        this._pattern = pattern
    }

    /**
     * 작동 시작 시간
     */
    private var _time_start: String? = null
    fun getTime_start(): String? = _time_start
    fun setTime_start(time_start: String?) {
        this._time_start = time_start
    }

    /**
     * 작동 종료 시간
     */
    private var _time_end: String? = null
    fun getTime_end(): String? = _time_end
    fun setTime_end(time_end: String?) {
        this._time_end = time_end
    }

    /**
     * 블루라이트 설정 값
     */
    private var _bluelight: String? = null
    fun getBluelight(): String? = _bluelight
    fun setBluelight(bluelight: String?) {
        this._bluelight = bluelight
    }

    /**
     * 상태바 아이콘 설정
     */
    private var _statusicon: String? = null
    fun getStatusicon(): String? = _statusicon
    fun setStatusicon(statusicon: String?) {
        this._statusicon = statusicon
    }

    /**
     * SMS 수신시 잠금 문자
     */
    private var _smslock: String? = null
    fun getSmslock(): String? = _smslock
    fun setSmslock(smslock: String?) {
        this._smslock = smslock
    }

    /**
     * SMS UnLock 메시지
     */
    private var _smsunlockmsg: String? = null
    fun getSmsunlockmsg(): String? = _smsunlockmsg
    fun setSmsunlockmsg(smsunlockmsg: String?) {
        this._smsunlockmsg = smsunlockmsg
    }

    /**
     * 0|0|0|1|0|1|0   <- 월|화|수|목|금|토|일  , 0 : 선택안됨, 1 : 선택됨
     */
    private var _prior_value: String? = null
    fun getPrior_value(): String? = _prior_value
    fun setPrior_value(prior_value: String?) {
        this._prior_value = prior_value
    }

    /**
     * 남은 시간 표시여부
     */
    private var _option1: Boolean = false
    fun isOption1(): Boolean = _option1
    fun setOption1(option1: Boolean) {
        this._option1 = option1
    }

    /**
     * 설정화면 잠금
     */
    private var _option2: Boolean = false
    fun isOption2(): Boolean = _option2
    fun setOption2(option2: Boolean) {
        this._option2 = option2
    }

    /**
     * 무작위 배경 화면
     */
    private var _option3: Boolean = false
    fun isOption3(): Boolean = _option3
    fun setOption3(option3: Boolean) {
        this._option3 = option3
    }

    /**
     * 잠금 활성화 여부
     */
    private var _option4: Boolean = false
    fun isOption4(): Boolean = _option4
    fun setOption4(option4: Boolean) {
        this._option4 = option4
    }

    /**
     * 타임가이드 삭제보호
     */
    private var _option5: Boolean = false
    fun isOption5(): Boolean = _option5
    fun setOption5(option5: Boolean) {
        this._option5 = option5
    }

    /**
     * 데몬 설치
     */
    private var _option6: Boolean = false
    fun isOption6(): Boolean = _option6
    fun setOption6(option6: Boolean) {
        this._option6 = option6
    }

    /**
     * 부팅시 자동 시작
     */
    private var _option7: Boolean = false
    fun isOption7(): Boolean = _option7
    fun setOption7(option7: Boolean) {
        this._option7 = option7
    }

    /**
     *
     */
    private var _option8: Boolean = false
    fun isOption8(): Boolean = _option8
    fun setOption8(option8: Boolean) {
        this._option8 = option8
    }

    /**
     * 신규설치 앱 잠금
     */
    private var _option9: Boolean = false
    fun isOption9(): Boolean = _option9
    fun setOption9(option9: Boolean) {
        this._option9 = option9
    }

    /**
     * 잠금 활성 시간 적용 여부
     * 잠금 해제 활성화 여부(true:활성화_암호 키패드 보이기, false:비활성화_암호 키패드 숨기기)
     */
    private var _option10: Boolean = false
    fun isOption10(): Boolean = _option10
    fun setOption10(option10: Boolean) {
        this._option10 = option10
    }
}