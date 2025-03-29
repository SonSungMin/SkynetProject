package com.skynet.skytimelock.adapter

import android.content.Context
import android.content.DialogInterface.OnDismissListener
import android.graphics.Bitmap
import android.os.Environment
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.skynet.common.SkyNetImageUtils
import com.skynet.common.SkyTimeLockCommon
import com.skynet.common.SkyTimeLockSetting
import com.skynet.skytimelock.free.R
import java.io.File
import java.lang.ref.WeakReference

class SkyImageListAdapter(private var mContext: Context?, private val default_chk: Int = 8) : BaseAdapter() {
    private var IMG_WIDTH: Int = 0
    private var IMG_HEIGHT: Int = 0
    private lateinit var common: SkyTimeLockCommon
    private lateinit var setting: SkyTimeLockSetting
    private var _listener: OnDismissListener? = null

    private var thumbsIDList: ArrayList<String> = ArrayList()
    private var checkedItem: ArrayList<String> = ArrayList()
    private var mRecycleList: MutableList<WeakReference<View>> = ArrayList()

    private var viewHolder: ViewHolder? = null
    private var inflater: LayoutInflater? = null

    companion object {
        private const val FILE_PATH = "${Environment.getExternalStorageDirectory()}/skynet/images"
    }

    private class ViewHolder {
        var selected_bg: TextView? = null
        var imageView: ImageView? = null
        var chkImage: CheckBox? = null
    }

    init {
        mContext?.let {
            inflater = LayoutInflater.from(it)
            getThumbInfo(thumbsIDList)

            common = SkyTimeLockCommon(it)
            setting = common.getSetting("SkyImageListAdapter")

            // 화면 사이즈 초기화
            val display = (it.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            IMG_WIDTH = display.width
            IMG_HEIGHT = display.height
        }
    }

    fun getSelectThumb(selectedIndex: Int): String {
        return if (thumbsIDList.isEmpty()) "" else thumbsIDList[selectedIndex]
    }

    fun deleteSelected() {
        for (item in checkedItem) {
            deleteSelected(item.toInt())
        }
    }

    @Synchronized
    fun deleteSelected(sIndex: Int) {
        try {
            val path = thumbsIDList[sIndex]
            common.setLogMsg("### SKY ### deleteSelected : $path")
            val f = File(path)

            if (f.exists()) {
                f.delete()
            }
        } catch (e: Exception) {
            common.setLogMsg("deleteSelected : ${e.message}")
        }
    }

    fun getCheckedCount(): Int {
        return checkedItem.size
    }

    override fun getCount(): Int {
        return thumbsIDList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setCheckVisible(value: Int) {
        viewHolder?.chkImage?.visibility = value
    }

    fun getCheckVisible(): Int {
        return viewHolder?.chkImage?.visibility ?: View.VISIBLE
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater?.inflate(R.layout.list_image_cell, null)

            v?.let { view ->
                viewHolder?.selected_bg = view.findViewById(R.id.selected_bg)
                viewHolder?.imageView = view.findViewById(R.id.ivImage)
                viewHolder?.chkImage = view.findViewById(R.id.chkImage)

                viewHolder?.chkImage?.setOnClickListener(clickListener)
                viewHolder?.chkImage?.tag = position
                setCheckVisible(default_chk)

                val imgLayout = viewHolder?.imageView?.layoutParams
                imgLayout?.width = (IMG_WIDTH / 3) - 5
                imgLayout?.height = (IMG_HEIGHT / 5) - 5
                viewHolder?.imageView?.layoutParams = imgLayout

                viewHolder?.selected_bg?.visibility = View.INVISIBLE
                view.tag = viewHolder
            }
        } else {
            viewHolder = v.tag as ViewHolder
        }

        try {
            val resized = common.getThumbnailImg(thumbsIDList[position], IMG_WIDTH, IMG_HEIGHT)

            if (setting.getBg() == thumbsIDList[position]) {
                viewHolder?.selected_bg?.visibility = View.VISIBLE
            } else {
                viewHolder?.selected_bg?.visibility = View.INVISIBLE
            }

            viewHolder?.imageView?.setImageBitmap(resized)
        } catch (e: OutOfMemoryError) {
            if (thumbsIDList.size <= parent.childCount) {
                common.setLogMsg("ImageListAdapter :: ${thumbsIDList.size} <= ${parent.childCount}")
                throw e
            }

            recycleHalf()
            System.gc()

            return getView(position, v, parent)
        }

        return v!!
    }

    private val clickListener = View.OnClickListener { v ->
        if ((v as CheckBox).isChecked) {
            checkedItem.add(v.tag.toString())
        } else {
            if (checkedItem.contains(v.tag.toString())) {
                checkedItem.remove(v.tag.toString())
            }
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        free()
        super.finalize()
    }

    private fun free() {
        inflater = null
        thumbsIDList.clear()
        mRecycleList.clear()
        viewHolder = null
        mContext = null
    }

    private fun getThumbInfo(thumbsIDs: ArrayList<String>) {
        try {
            val file = File(FILE_PATH)
            if (!file.exists()) return

            val files = file.listFiles()
            files?.forEach {
                thumbsIDs.add(it.absolutePath)
            }
        } catch (e: Exception) {
            common.setLogMsg("getThumbInfo :: ${e.message}")
        }
    }

    fun recycleHalf() {
        val halfSize = mRecycleList.size / 2
        val recycleHalfList = mRecycleList.subList(0, halfSize)
        SkyNetImageUtils.recursiveRecycle(recycleHalfList)

        repeat(halfSize) {
            mRecycleList.removeAt(0)
        }
    }
}