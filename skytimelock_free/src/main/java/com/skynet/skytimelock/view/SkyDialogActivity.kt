package com.skynet.skytimelock.view

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast

import com.skynet.skytimelock.free.R

class SkyDialogActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val PKGNAME = intent.getStringExtra("PKGNAME")

        // 세로 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        try {
            setContentView(R.layout.dialog_view)

            val ab = AlertDialog.Builder(this@SkyDialogActivity)
            ab.setIcon(android.R.drawable.ic_dialog_alert)
            ab.setTitle("Sorry!")
            ab.setMessage("The application was terminated.\n(process $PKGNAME) has stopped unexpectedly. Please try again.")
            ab.setPositiveButton("Force close", OkOnClickListener)
            ab.show()
        } catch(e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private val OkOnClickListener = OnClickListener { _, _ ->
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}