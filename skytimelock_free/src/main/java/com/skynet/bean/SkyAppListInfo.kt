package com.skynet.skytimelock.bean

import android.graphics.drawable.Drawable

class SkyAppListInfo {
    private var APP_POSITION: Int = 0
    private var APP_NAME: String? = null
    private var APP_PKG_NAME: String? = null
    private var APP_DESC: String? = null
    private var APP_GBN: String? = null
    private var APP_LMTTIME: String? = null
    private var APP_SPCTIME: String? = null
    private var APP_OPTION1: String? = null
    private var APP_OPTION2: String? = null
    private var APP_OPTION3: String? = null
    private var APP_OPTION4: String? = null
    private var APP_ICON: Drawable? = null
    private var APP_CHK: Boolean = false

    /**
     * 필수 잠금 어플
     */
    private var APP_REQUIRED: Boolean = false

    /**
     * 항상 허용 어플
     */
    private var APP_ALLOWED: Boolean = false

    var ISEMPTY: Boolean = false
        get() = field
        set(value) { field = value }

    // Getter와 Setter 메서드
    fun getAPP_POSITION(): Int = APP_POSITION
    fun setAPP_POSITION(aPP_POSITION: Int) { APP_POSITION = aPP_POSITION }

    fun getAPP_NAME(): String? = APP_NAME
    fun setAPP_NAME(aPP_NAME: String?) { APP_NAME = aPP_NAME }

    fun getAPP_PKG_NAME(): String? = APP_PKG_NAME
    fun setAPP_PKG_NAME(aPP_PKG_NAME: String?) { APP_PKG_NAME = aPP_PKG_NAME }

    fun getAPP_DESC(): String? = APP_DESC
    fun setAPP_DESC(aPP_DESC: String?) { APP_DESC = aPP_DESC }

    fun getAPP_GBN(): String? = APP_GBN
    fun setAPP_GBN(aPP_GBN: String?) { APP_GBN = aPP_GBN }

    fun getAPP_LMTTIME(): String? = APP_LMTTIME
    fun setAPP_LMTTIME(aPP_LMTTIME: String?) { APP_LMTTIME = aPP_LMTTIME }

    fun getAPP_SPCTIME(): String? = APP_SPCTIME
    fun setAPP_SPCTIME(aPP_SPCTIME: String?) { APP_SPCTIME = aPP_SPCTIME }

    fun getAPP_OPTION1(): String? = APP_OPTION1
    fun setAPP_OPTION1(aPP_OPTION1: String?) { APP_OPTION1 = aPP_OPTION1 }

    fun getAPP_OPTION2(): String? = APP_OPTION2
    fun setAPP_OPTION2(aPP_OPTION2: String?) { APP_OPTION2 = aPP_OPTION2 }

    fun getAPP_OPTION3(): String? = APP_OPTION3
    fun setAPP_OPTION3(aPP_OPTION3: String?) { APP_OPTION3 = aPP_OPTION3 }

    fun getAPP_OPTION4(): String? = APP_OPTION4
    fun setAPP_OPTION4(aPP_OPTION4: String?) { APP_OPTION4 = aPP_OPTION4 }

    fun getAPP_ICON(): Drawable? = APP_ICON
    fun setAPP_ICON(aPP_ICON: Drawable?) { APP_ICON = aPP_ICON }

    fun getAPP_CHK(): Boolean = APP_CHK
    fun setAPP_CHK(aPP_CHK: Boolean) { APP_CHK = aPP_CHK }

    fun getAPP_REQUIRED(): Boolean = APP_REQUIRED
    fun setAPP_REQUIRED(aPP_REQUIRED: Boolean) { APP_REQUIRED = aPP_REQUIRED }

    fun getAPP_ALLOWED(): Boolean = APP_ALLOWED
    fun setAPP_ALLOWED(aPP_ALLOWED: Boolean) { APP_ALLOWED = aPP_ALLOWED }

    fun isISEMPTY(): Boolean = ISEMPTY
    fun setISEMPTY(iSEMPTY: Boolean) { ISEMPTY = iSEMPTY }
}