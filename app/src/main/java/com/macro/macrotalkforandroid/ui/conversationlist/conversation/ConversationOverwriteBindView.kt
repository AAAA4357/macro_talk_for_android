package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.macro.macrotalkforandroid.R

// 对话覆盖绑定视图，用于展示对话覆盖界面
class ConversationOverwriteBindView(val context: Context, val resources: Resources, val activity: ConversationActivity)
    : OnBindView<BottomDialog>(R.layout.conversation_overwrite) {

    lateinit var view: View

    override fun onBind(dialog: BottomDialog?, v: View?) {
        view = v!!
        val avator = v.findViewById<ImageButton>(R.id.overwrite_avator)
        avator.setOnClickListener(OnAvatorClick())
        val reset = v.findViewById<TextView>(R.id.overwrite_avator_reset)
        reset.setOnClickListener(OnAvatorResetClick())
    }

    // 加载头像
    fun loadAvator(bitmap: Bitmap?) {
        val avator = view.findViewById<ImageButton>(R.id.overwrite_avator)
        if (bitmap == null) {
            avator.setImageDrawable(resources.getDrawable(R.drawable.ic_addimage))
        } else {
            avator.setImageBitmap(bitmap)
        }
    }

    // 加载名称
    fun loadName(content: String?) {
        val name = view.findViewById<EditText>(R.id.overwrite_name)
        if (content == null) {
            name.setText("")
        } else {
            name.setText(content)
        }
    }

    // 点击头像事件
    inner class OnAvatorClick : OnClickListener {
        override fun onClick(v: View?) {
            // 请求权限
            XXPermissions.with(context)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(object : OnPermissionCallback {

                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        // 打开系统相册
                        val intent = Intent(Intent.ACTION_PICK, null)
                        intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*"
                        )
                        activity.startActivityForResult(intent, 2)
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(
                                context,
                                "请手动授予权限并重新上传图片",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(context, "请授予权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    // 点击重置头像事件
    inner class OnAvatorResetClick : OnClickListener {
        override fun onClick(v: View?) {
            val avator = view.findViewById<ImageButton>(R.id.overwrite_avator)
            avator.setImageDrawable(resources.getDrawable(R.drawable.ic_addimage))
            activity.avatorOverwrite = null
        }
    }
}
