package com.macro.macrotalkforandroid.ui.conversationlist

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.ProfileSelector
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.ceil
import kotlin.math.sqrt

class AddConversationBindView(val resources : Resources, val context : Context, val fragment : ConversationListFragment, val isRewrite : Boolean = false) : OnBindView<CustomDialog>(
    R.layout.fragment_addconversation
) {
    // 会话在列表中的索引（用于修改会话时）
    var index: Int = -1

    // 布局中的视图
    lateinit var view : View

    // 封面图片按钮
    lateinit var cover : ImageButton

    // 用户配置文件适配器
    lateinit var profileAdapter : AddConversationProfileAdapter

    // 搜索输入框
    lateinit var searchText : EditText

    // 会话封面图片路径
    var conversationCover : String? = null

    // 绑定视图
    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        cover = view.findViewById(R.id.conversation_cover)
        cover.setOnClickListener(OnCoverUploadClick())
        val coverClear = view.findViewById<TextView>(R.id.add_conversation_cover_reset)
        coverClear.setOnClickListener(OnCoverClearClick())
        profileAdapter = AddConversationProfileAdapter(context, Utils.prefabData.Profiles + Utils.storageData.Profiles)
        searchText = view.findViewById(R.id.add_conversation_search_content)
        searchText.addTextChangedListener(OnSearchTextChanged())
        val searchClear = view.findViewById<TextView>(R.id.add_conversation_search_reset)
        searchClear.setOnClickListener(OnSearchTextClearClick())
        if (!isRewrite) {
            val list = view.findViewById<RecyclerView>(R.id.add_conversation_profiles)
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val itemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
            list.apply {
                this.adapter = profileAdapter
                this.layoutManager = layoutManager
                this.addItemDecoration(itemDecoration)
            }
        } else {
            val list = view.findViewById<RecyclerView>(R.id.add_conversation_profiles)
            list.visibility = View.GONE
        }
        val tags = view.findViewById<EditText>(R.id.add_conversation_tags)
        tags.hint = resources.getString(R.string.add_tags, Utils.SettingData.DefaultSplitChar)
        val cancel = view.findViewById<TextView>(R.id.add_conversation_cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = view.findViewById<TextView>(R.id.add_conversation_confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog))
    }

    // 加载会话
    fun loadConversation(conversation : Conversation) {
        val title = view.findViewById<EditText>(R.id.add_conversation_title)
        title.setText(conversation.Title)
        val selectedProfiles = conversation.Profiles.map { it.toProfile() }
        profileAdapter.selectedProfiles = selectedProfiles
        val list = view.findViewById<RecyclerView>(R.id.add_conversation_profiles)
        list.adapter = null
        list.adapter = profileAdapter
        val tags = view.findViewById<EditText>(R.id.add_conversation_tags)
        tags.setText(conversation.Tags?.joinToString(Utils.SettingData.DefaultSplitChar))
    }

    // 上传图片
    fun uploadImage(bitmap : Bitmap, path : String) {
        cover.setImageBitmap(bitmap)
        conversationCover = path
    }

    // 封面图片上传点击事件监听器
    inner class OnCoverUploadClick : OnClickListener {
        override fun onClick(v: View?) {
            XXPermissions.with(context)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(object : OnPermissionCallback {

                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        val intent = Intent(Intent.ACTION_PICK,  null)
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                        fragment.startActivityForResult(intent, 1)
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(context, "请手动授予权限并重新上传图片", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "请授予权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    // 清除封面图片点击事件监听器
    inner class OnCoverClearClick : OnClickListener {
        override fun onClick(v: View?) {
            conversationCover = null
            cover.setImageDrawable(resources.getDrawable(R.drawable.ic_addimage))
        }
    }

    // 清除搜索文本点击事件监听器
    inner class OnSearchTextClearClick : OnClickListener {
        override fun onClick(v: View?) {
            searchText.setText("")
        }
    }

    // 搜索文本改变事件监听器
    inner class OnSearchTextChanged : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val searchKey = s?.toString() ?: return
            profileAdapter.filter.filter(searchKey)
        }
    }

    // 取消按钮点击事件监听器
    inner class OnCancelClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    // 确认按钮点击事件监听器
    inner class OnConfirmClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            val title = view.findViewById<EditText>(R.id.add_conversation_title)
            val profiles = profileAdapter.selectedProfiles
            if (profiles.isEmpty() && !isRewrite) {
                PopTip.show("请选择至少一份档案")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
                return
            }
            val tags = view.findViewById<EditText>(R.id.add_conversation_tags)

            // 生成会话标题
            val titleText = if (title.text.toString() == "") if (profiles.size <= 2) if (profiles.size == 1) profiles[0].Name else "组（" + profiles.map{ it.Name }.joinToString("，") + "）" else "组（${profiles[0].Name}，...，${profiles[profiles.size - 1].Name}）" else title.text.toString()

            // 准备会话封面图片
            var image : Image? = null
            if (conversationCover != null) {
                val file = File(conversationCover!!)
                val newfile = File(Utils.appDataPath + "/" + Utils.toMD5(file.name))
                if (newfile.exists()) {
                    Files.copy(file.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.copy(file.toPath(), newfile.toPath())
                }
                image = Image(title.text.toString(), newfile.absolutePath, true)
            }

            // 如果没有封面图片，生成默认封面图片
            if (image == null) {
                val gridLayout = GridLayout(context)
                val gridWidth = ceil(sqrt(profiles.size.toDouble())).toInt()
                gridLayout.columnCount = gridWidth
                for (i : Int in 0..profiles.size - 1) {
                    val image = ImageView(context)
                    if (!profiles[i].Images[0].isNotPrefab) {
                        image.setImageBitmap(BitmapFactory.decodeStream(context.assets.open(profiles[i].Images[0].ImageName + ".jpg")))
                    } else {
                        image.setImageBitmap(BitmapFactory.decodeFile(profiles[i].Images[0].ImageOriginalUri))
                    }
                    val params = LinearLayout.LayoutParams(
                        Utils.dip2px(context, 40f),
                        Utils.dip2px(context, 40f)
                    )
                    image.layoutParams = params
                    gridLayout.addView(image)
                }

                gridLayout.setBackgroundColor(Color.WHITE)
                gridLayout.layout(0, 0, gridWidth * Utils.dip2px(
                    context,
                    40f
                ), gridWidth * Utils.dip2px(context, 40f)
                )
                val measuredWidth =
                    View.MeasureSpec.makeMeasureSpec(gridWidth * Utils.dip2px(context, 40f), View.MeasureSpec.EXACTLY)
                val measuredHeight =
                    View.MeasureSpec.makeMeasureSpec(gridWidth * Utils.dip2px(context, 40f), View.MeasureSpec.EXACTLY)
                gridLayout.measure(measuredWidth, measuredHeight)
                gridLayout.layout(0, 0, gridLayout.measuredWidth, gridLayout.measuredHeight)

                val viewWidth = gridLayout.measuredWidth
                val viewHeight = gridLayout.measuredHeight
                val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
                val cvs = Canvas(bitmap)
                gridLayout.draw(cvs)

                val saveFile = File(Utils.appDataPath, titleText)
                try {
                    val saveImgOut = FileOutputStream(saveFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut)
                    saveImgOut.flush()
                    saveImgOut.close()
                } catch (_ : Exception) {}
                image = Image(titleText + "_Cover", Utils.appDataPath + "/" + titleText, true)
            }

            // 创建会话对象并添加到列表中
            val conversation = Conversation(
                titleText,
                profiles.map {
                    if (Utils.storageData.Profiles.contains(it)) {
                        ProfileSelector(false, Utils.storageData.Profiles.indexOf(it))
                    } else {
                        ProfileSelector(true, Utils.prefabData.Profiles.indexOf(it))
                    }
                },
                image,
                if (tags.text.toString() == "") null else tags.text.toString().split(Utils.SettingData.DefaultSplitChar)
            )
            fragment.addConversation(conversation)
            dialog.dismiss()
        }
    }
}
