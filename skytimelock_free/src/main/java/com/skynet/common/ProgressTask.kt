package com.skynet.common

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.skynet.skytimelock.free.R

// 다음 import 줄을 프로젝트의 실제 R 클래스 위치로 변경해주세요
// import com.skynet.skytimelock.free.R

class ProgressTask(private val mContext: Context) : AsyncTask<Int, String, Int>() {
    private lateinit var mDlg: ProgressDialog

    override fun onPreExecute() {
        mDlg = ProgressDialog(mContext)
        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mDlg.show()
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Int?): Int {
        val taskCnt = params[0] ?: 0
        publishProgress("max", taskCnt.toString())

        for (i in 0 until taskCnt) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            publishProgress("progress", i.toString(), "$i Updated.")
        }
        return taskCnt
    }

    override fun onProgressUpdate(vararg progress: String) {
        when (progress[0]) {
            "progress" -> {
                mDlg.setProgress(progress[1].toInt())
                mDlg.setMessage(progress[2])
            }
            "max" -> {
                mDlg.setMax(progress[1].toInt())
            }
        }
    }

    override fun onPostExecute(result: Int) {
        mDlg.dismiss()
        // R 클래스 참조 부분을 직접 접근하도록 수정
        Toast.makeText(mContext, mContext.getString(R.string.apply_ok) + " (" + result + ")", Toast.LENGTH_SHORT).show()
    }
}