package com.skynet.skytimelock.view

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle

import com.skynet.skytimelock.free.R

class SkySettingDialogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_app_set)
    }

    override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)

        // no background panel is shown
        theme.applyStyle(android.R.style.Theme_Panel, true)
    }
}