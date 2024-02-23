package com.Macro.macrotalkforandroid

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.Macro.macrotalkforandroid.ui.conversationlist.ConversationListFragment
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.ceil
import kotlin.math.sqrt


class AddConversationBindView(val resources : Resources, val context : Context, val fragment : ConversationListFragment) : OnBindView<CustomDialog>(R.layout.fragment_addconversation) {

    lateinit var view : View

    lateinit var cover : ImageButton

    lateinit var profileAdapter : AddConversationProfileAdapter

    lateinit var searchText : EditText

    var conversationCover : String? = null

    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        cover = view.findViewById<ImageButton>(R.id.add_conversation_cover)
        cover.setOnClickListener(OnCoverUploadClick())
        profileAdapter = AddConversationProfileAdapter(context, Utils.prefabData.Profiles + Utils.storageData.Profiles)
        searchText = view.findViewById(R.id.add_conversation_search_content)
        searchText.addTextChangedListener(OnSearchTextChanged())
        val searchClear = view.findViewById<TextView>(R.id.add_conversation_search_reset)
        searchClear.setOnClickListener(OnSearchTextClearClick())
        val list = view.findViewById<RecyclerView>(R.id.add_conversation_profiles)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val itemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        list.apply {
            this.adapter = profileAdapter
            this.layoutManager = layoutManager
            this.addItemDecoration(itemDecoration)
        }
        val tags = view.findViewById<EditText>(R.id.add_conversation_tags)
        tags.hint = resources.getString(R.string.add_tags, Utils.SettingData.DefaultSplitChar)
        val cancel = view.findViewById<TextView>(R.id.add_conversation_cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = view.findViewById<TextView>(R.id.add_conversation_confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog))
    }

    fun loadConversation(conversation : Conversation) {
        val title = view.findViewById<EditText>(R.id.add_conversation_title)
        title.setText(conversation.Title)
        val selectedProfiles = conversation.Profiles.map {
            if (it.isPrefab) {
                Utils.prefabData.Profiles[it.PrefabIndex]
            } else {
                Utils.storageData.Profiles[it.PrefabIndex]
            }
        }
        profileAdapter.selectedProfiles = selectedProfiles
        val list = view.findViewById<RecyclerView>(R.id.add_conversation_profiles)
        list.adapter = null
        list.adapter = profileAdapter
        val tags = view.findViewById<EditText>(R.id.add_conversation_tags)
        tags.setText(conversation.Tags?.joinToString(Utils.SettingData.DefaultSplitChar))
    }

    fun uploadImage(bitmap : Bitmap, path : String) {
        cover.setImageBitmap(bitmap)
        conversationCover = path
    }

    inner class OnCoverUploadClick : OnClickListener {
        override fun onClick(v: View?) {
            XXPermissions.with(context)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(object : OnPermissionCallback {

                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        uploadImage()
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

        fun uploadImage() {
            val intent = Intent(Intent.ACTION_PICK,  null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            fragment.startActivityForResult(intent, 1)
        }
    }

    inner class OnSearchTextClearClick : OnClickListener {
        override fun onClick(v: View?) {
            searchText.setText("")
        }
    }

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

    inner class OnCancelClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    inner class OnConfirmClick(val dialog : CustomDialog) : OnClickListener {
        override fun onClick(v: View?) {
            val title = view.findViewById<EditText>(R.id.add_conversation_title)
            val profiles = profileAdapter.selectedProfiles
            if (profiles.isEmpty()) {
                PopTip.show("请选择至少一份档案")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
            }
            val tags = view.findViewById<EditText>(R.id.add_conversation_tags)

            val titleText = if (title.text.toString() == "") "组（" + profiles.map{ it.Name }.joinToString("，") + "）" else title.text.toString()

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

            if (image == null) {
                val gridLayout = GridLayout(context)
                gridLayout.columnCount = ceil(sqrt(profiles.size.toDouble())).toInt()
                for (i : Int in 0..profiles.size - 1) {
                    val image = ImageView(context)
                    if (profiles[i].Images[0].isNotPrefab) {
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
                gridLayout.isDrawingCacheEnabled = true
                gridLayout.buildDrawingCache(true)
                val bitmap = Bitmap.createBitmap(gridLayout.drawingCache)
                gridLayout.isDrawingCacheEnabled = false

                image = Image(titleText + "_Cover", "", true)
            }

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

            dialog.dismiss()
        }
    }
}