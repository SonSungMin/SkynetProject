package com.skynet.skytimelock.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.Display
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.Toast

import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.skytimelock.adapter.SkyImageListAdapter
import com.skynet.skytimelock.free.R
import java.io.File
import java.io.IOException
import java.util.Calendar

class SkyLockImageListView : Activity() {
    private var progressDialog: ProgressDialog? = null

    private var IMG_WIDTH: Int = 0
    private var IMG_HEIGHT: Int = 0
    private var SCREEN_WIDTH: Int = 0
    private var SCREEN_HEIGHT: Int = 0

    private lateinit var imageAdapterSDCard: SkyImageListAdapter

    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting
    private lateinit var btn_add_img: Button
    private lateinit var btn_del_img: Button

    companion object {
        const val REQ_CODE_PICK_IMAGE = 0
        const val PHOTO_FILE = "skynet_timelock_bg_"  // 임시 저장파일
    }

    private var PHOTO_FILE_PATH = "${Environment.getExternalStorageDirectory()}/skynet/images"
    private var galleryFilePath: Array<String>? = null

    private var default_chk = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.list_image)

        // 화면 사이즈 초기화
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        SCREEN_WIDTH = display.width
        SCREEN_HEIGHT = display.height
        IMG_WIDTH = SCREEN_WIDTH / 3
        IMG_HEIGHT = IMG_WIDTH

        common = SkyTimeLockCommon(this)
        setting = common.getSetting("SkyLockImageListView")

        initGallary(8)

        btn_add_img = findViewById(R.id.btn_add_new_img)
        btn_add_img.setOnClickListener(btn_clickEvent)
        btn_del_img = findViewById(R.id.btn_delete_img)
        btn_del_img.setOnClickListener(btn_clickEvent)
    }

    private val btn_clickEvent = OnClickListener { v ->
        when (v.id) {
            // 갤러리 이미지 추가
            R.id.btn_add_new_img -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                intent.type = "image/*"                                         // 모든 이미지
                intent.putExtra("crop", "true")                                 // Crop기능 활성화
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri())          // 임시파일 생성
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()) // 포맷방식

                intent.putExtra("aspectX", SCREEN_WIDTH)
                intent.putExtra("aspectY", SCREEN_HEIGHT)
                intent.putExtra("outputX", SCREEN_WIDTH)
                intent.putExtra("outputY", SCREEN_HEIGHT)

                startActivityForResult(intent, REQ_CODE_PICK_IMAGE)
            }

            // 갤러리 이미지 삭제
            R.id.btn_delete_img -> {
                if (imageAdapterSDCard.getCheckVisible() == 8) {
                    Toast.makeText(this@SkyLockImageListView, getString(R.string.dialog_no_selected), Toast.LENGTH_SHORT).show()
                    initGallary(0)
                } else {
                    delGalleryFile()
                }
            }
        }
    }

    /**
     * 갤러리 파일 삭제
     */
    private fun delGalleryFile() {
        if (imageAdapterSDCard.getCheckedCount() == 0) {
            Toast.makeText(applicationContext, getString(R.string.dialog_no_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val alt_bld = AlertDialog.Builder(this)
        alt_bld.setMessage(getString(R.string.dialog_delete_image_msg))
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                imageAdapterSDCard.deleteSelected()
                Toast.makeText(this@SkyLockImageListView, getString(R.string.dialog_delete_ok_msg), Toast.LENGTH_SHORT).show()
                initGallary(0)
            }
            .setNegativeButton("No") { dialog, _ ->
                // Action for 'NO' Button
                dialog.cancel()
            }
        val alert = alt_bld.create()
        // Title for AlertDialog
        alert.setTitle(getString(R.string.dialog_delete_image))
        // Icon for AlertDialog
        alert.setIcon(R.drawable.main_icon)
        alert.show()
    }

    /** 임시 저장 파일의 경로를 반환 */
    private fun getTempUri(): Uri {
        return Uri.fromFile(getTempFile())
    }

    /**
     * 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환
     */
    private fun getTempFile(): File? {
        if (isSDCARDMOUNTED()) {
            val photo_file = PHOTO_FILE + Calendar.getInstance().get(Calendar.MILLISECOND) + ".jpg"

            val f = File(common.PHOTO_FILE_PATH, photo_file)
            val fd = File(common.PHOTO_FILE_PATH)

            try {
                if (!fd.exists()) f.mkdirs()

                // 외장메모리에 temp.jpg 파일 생성
                f.createNewFile()
            } catch (e: IOException) {
                Log.e("", "### SkyNet getTempFile error : $e")
            }

            return f
        } else {
            return null
        }
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private fun isSDCARDMOUNTED(): Boolean {
        val status = Environment.getExternalStorageState()
        return status == Environment.MEDIA_MOUNTED
    }

    /** 다시 액티비티로 복귀하였을때 이미지를 셋팅 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        when (requestCode) {
            REQ_CODE_PICK_IMAGE -> {
                if (resultCode == RESULT_OK && imageData != null) {
                    val filePath = common.PHOTO_FILE_PATH + "/" + PHOTO_FILE

                    // 배경 변경
                    setting.setBg(filePath)

                    saveSetting()

                    // 갤러리 갱신
                    initGallary(8)
                } else {
                    delGalleryFile()
                }
            }
        }
    }

    /**
     * 환경 설정 저장
     */
    private fun saveSetting() {
        common.setSetting(setting)
    }

    /**
     * 갤러리 초기화
     */
    @Synchronized
    private fun initGallary(_chk_visible: Int) {
        showWaitProg()
        default_chk = _chk_visible
        Thread {
            try {
                imageAdapterSDCard = SkyImageListAdapter(this@SkyLockImageListView, default_chk)
            } catch (e: Exception) {
                Log.e("", "### $e")
            }
            mHandler.sendMessage(Message.obtain(mHandler, 1))
        }.start()
    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                // 이미지 리스트
                1 -> setImageListBinding()
            }
            hideWaitProg()
        }
    }

    @SuppressLint("InlinedApi")
    private fun setImageListBinding() {
        val gv = findViewById<GridView>(R.id.ImgGridView)

        imageAdapterSDCard = SkyImageListAdapter(this, default_chk)
        gv.adapter = imageAdapterSDCard
        gv.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val imgPath = imageAdapterSDCard.getSelectThumb(position)

            val alt_bld = AlertDialog.Builder(this@SkyLockImageListView)
            alt_bld.setMessage(getString(R.string.dialog_choose_image_msg))
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    setting.setBg(imgPath)
                    common.setSetting(setting)

                    initGallary(default_chk)
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Action for 'NO' Button
                    dialog.cancel()
                }
            val alert = alt_bld.create()
            // Title for AlertDialog
            alert.setTitle(getString(R.string.dialog_choose_image))
            // Icon for AlertDialog
            alert.setIcon(R.drawable.main_icon)
            alert.show()
        }
    }

    /**
     * Progress 보이기
     */
    private fun showWaitProg() {
        showWaitProg(null)
    }

    /**
     * Progress 보이기
     * @param msg
     */
    private fun showWaitProg(msg: String?) {
        progressDialog = ProgressDialog.show(
            this@SkyLockImageListView,
            "",
            if (msg == null || msg == "") "처리중입니다.\n잠시만 기다려주세요." else msg,
            true,
            true
        )
    }

    /**
     * Progress 숨기기
     */
    private fun hideWaitProg() {
        progressDialog?.dismiss()
    }
}