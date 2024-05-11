package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import java.io.ByteArrayOutputStream


class ConversationPreviewBindView(val context: Context)
    : OnBindView<CustomDialog>(R.layout.fragment_export_preview) {
    lateinit var view: View

    lateinit var upperDialog: CustomDialog

    // 存储预览图像的位图列表
    var bitmaps = listOf<Bitmap>()

    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        val adapter = PreviewAdapter()
        adapter.list = bitmaps
        val list = view.findViewById<RecyclerView>(R.id.preview_list)
        list.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        val cancel = view.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = view.findViewById<TextView>(R.id.confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog))
    }

    // 取消按钮点击监听器
    inner class OnCancelClick(val dialog: CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    // 确定按钮点击监听器
    inner class OnConfirmClick(val dialog: CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            for (bitmap in bitmaps) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val datas = baos.toByteArray()

                Utils.saveImageToGallery(context, datas)
            }
            upperDialog.dismiss()
            dialog.dismiss()
        }
    }

    inner class PreviewAdapter : Adapter<PreviewAdapter.PreviewViewHolder>() {
        var list : List<Bitmap> = listOf()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PreviewViewHolder {
            val viewHolder = PreviewViewHolder(LayoutInflater.from(context).inflate(R.layout.preview_item, parent, false))
            viewHolder.previewView = viewHolder.itemView.findViewById(R.id.preview)
            return  viewHolder
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
            val preview = holder.previewView
            preview.setImageBitmap(list[position])
        }

        inner class PreviewViewHolder(itemView : View) : ViewHolder(itemView) {
            lateinit var previewView : ImageView
        }
    }
}
