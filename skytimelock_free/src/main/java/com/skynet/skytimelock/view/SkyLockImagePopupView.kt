package com.skynet.skytimelock.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView

import com.skynet.skytimelock.free.R

class SkyLockImagePopupView : Activity() {
    private lateinit var iv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_image_popup)

        iv = findViewById(R.id.imageView)

        val i = intent
        val extras = i.extras
        val imgPath = extras?.getString("filename")

        val bfo = BitmapFactory.Options().apply {
            inScaled = true
            inDensity = DisplayMetrics.DENSITY_HIGH
            inTargetDensity = resources.displayMetrics.densityDpi
            inSampleSize = 2
        }

        val buttonImages = BitmapFactory.decodeFile(imgPath, bfo)
        iv.setImageBitmap(buttonImages)
    }
}