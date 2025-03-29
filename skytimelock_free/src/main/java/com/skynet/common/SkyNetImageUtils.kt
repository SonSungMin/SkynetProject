package com.skynet.common

import java.lang.ref.WeakReference
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView

object SkyNetImageUtils {
    /**
     * 뷰와 그 하위 뷰들을 재귀적으로 정리하는 함수
     */
    fun recursiveRecycle(root: View?) {
        if (root == null)
            return

        root.setBackgroundDrawable(null)

        if (root is ViewGroup) {
            val group = root
            val count = group.childCount
            for (i in 0 until count) {
                recursiveRecycle(group.getChildAt(i))
            }
            if (root !is AdapterView<*>) {
                group.removeAllViews()
            }
        }

        if (root is ImageView) {
            root.setImageDrawable(null)
        }

        return
    }

    /**
     * 뷰 리스트를 재귀적으로 정리하는 함수
     */
    fun recursiveRecycle(recycleList: List<WeakReference<View>>) {
        for (ref in recycleList) {
            recursiveRecycle(ref.get())
        }
    }

    /**
     * Bitmap 너비
     *
     * @param fileName 이미지 파일 경로
     * @return 비트맵 너비, 오류 시 0 반환
     */
    fun getBitmapOfWidth(fileName: String): Int {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(fileName, options)
            options.outWidth
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Bitmap 높이
     *
     * @param fileName 이미지 파일 경로
     * @return 비트맵 높이, 오류 시 0 반환
     */
    fun getBitmapOfHeight(fileName: String): Int {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(fileName, options)
            options.outHeight
        } catch (e: Exception) {
            0
        }
    }
}