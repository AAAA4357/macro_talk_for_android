package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.kongzue.dialogx.util.TextInfo
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.DialogueType
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinnerAdapter
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinnerBaseAdapter
import java.io.File
import java.lang.IndexOutOfBoundsException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ConversationActivity : AppCompatActivity() {
    companion object {
        lateinit var conversation: Conversation
    // 对话实例
    }

    var extraExpanded: Boolean = false
    // 额外展开标志

    lateinit var dialogueAdapter: DialogueListAdapter
    // 对话列表适配器
    lateinit var conversationOverwriteBindView: ConversationOverwriteBindView
    // 对话覆盖视图绑定
    var counter = 0
    // 计数器
    var imagecounter = 0
    // 图片计数器
    var avatorOverwrite: String? = null
    // 头像覆盖
    var nameOverwrite: String? = null
    // 名称覆盖
    var multiSelect: Boolean = false
    // 多选模式标志

    lateinit var personalButton : ImageButton

    lateinit var profileButton : ImageButton

    lateinit var avatorButton : ImageButton

    var personal = 0

    var profile : Int = 0
        set(value) {
            field = value
            avatorList = conversation.Profiles[value].toProfile().Images.map {
                it.toBitmap()
            }
            if (avatorList.count() == 1) {
                avatorButton.visibility = View.GONE
            } else {
                avatorButton.visibility = View.VISIBLE
            }
        }

    var avator = 0

    lateinit var profileList : List<String>

    lateinit var profileBitmapList : List<Bitmap>

    lateinit var avatorList : List<Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        personalButton = findViewById(R.id.dialogue_Personal)

        profileButton = findViewById(R.id.conversation_profile)

        avatorButton = findViewById(R.id.conversation_avator)

        personalButton.setOnClickListener(OnPersonalClick())

        if (conversation.Profiles.count() == 1) {
            profileButton.visibility = View.GONE
        } else {
            profileList = conversation.Profiles.map {
                it.toProfile().Name
            }
            profileBitmapList = conversation.Profiles.map {
                it.toProfile().Images[0].toBitmap()
            }
            profileButton.setOnClickListener(OnProfileClick())
            profileButton.setImageBitmap(profileBitmapList[0])
        }

        avatorList = conversation.Profiles[0].toProfile().Images.map{ it.toBitmap() }
        avatorAdapter = AvatorAdapter(avatorList)
        avatorButton.setImageBitmap(avatorList[0])
        avatorButton.setOnClickListener(OnAvatorClick())

        title = "对话 - " + conversation.Title

        val list = findViewById<RecyclerView>(R.id.Conversation_DialogueList)
        list.setBackgroundColor(Color.parseColor(Utils.SettingData.ConversationBgColor))

        val imageButton = findViewById<ImageButton>(R.id.conversation_cover)
        imageButton.setOnClickListener(OnExpandClick())

        val send = findViewById<TextView>(R.id.sender_send)
        send.setOnClickListener(OnSendClick())

        val knot = findViewById<ImageButton>(R.id.conversation_extra_knot)
        knot.setOnClickListener(OnKnotClick())

        val reply = findViewById<ImageButton>(R.id.conversation_extra_reply)
        reply.setOnClickListener(OnReplyClick())

        val image = findViewById<ImageButton>(R.id.conversation_extra_image)
        image.setOnClickListener(OnImageClick())

        val overwrite = findViewById<ImageButton>(R.id.conversation_extra_overwrite)
        overwrite.setOnClickListener(OnOverwriteClick())

        val dialogues = listOf<Dialogue>().toMutableList()

        conversation.Dialogues.forEach {
            dialogues.add(it)
        }

        dialogueAdapter = DialogueListAdapter(this@ConversationActivity, dialogues, resources)

        val itemTouchHelper = ItemTouchHelper(DialogueItemTouchHelper(this@ConversationActivity, dialogues, dialogueAdapter, conversation))
        itemTouchHelper.attachToRecyclerView(list)

        dialogueAdapter.setOnItemClickListener(OnDialogueItemClick())

        val layoutManager = LinearLayoutManager(this@ConversationActivity, RecyclerView.VERTICAL, false)
        list.apply {
            this.adapter = dialogueAdapter
            this.layoutManager = layoutManager
        }

        conversationOverwriteBindView = ConversationOverwriteBindView(this@ConversationActivity, resources, this@ConversationActivity)

        val export = findViewById<ImageButton>(R.id.conversation_export)
        export.setOnClickListener(OnExportClick())

        if (!Utils.SettingData.HintDisplyed) {
            Utils.ShowHint(4, listOf(personalButton, profileButton, avatorButton))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val file = Utils.uriToFile(this@ConversationActivity, data.data!!)!!

            val image = Image(Utils.toMD5(file.name), file.absolutePath, true)

            if (conversation.Dialogues.size > 0) {
                if (conversation.Dialogues[conversation.Dialogues.size - 1].Avator != conversation.Profiles[profile].toProfile().Images[avator]) imagecounter = 0
            }

            val overwriteImage = if (avatorOverwrite == null) {
                null
            } else {
                val file = File(avatorOverwrite!!)
                val newfile = File(Utils.appDataPath + "/" + Utils.toMD5(file.name))
                if (newfile.exists()) {
                    Files.copy(file.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.copy(file.toPath(), newfile.toPath())
                }
                Image(Utils.toMD5(file.name), newfile.absolutePath, true)
            }

            addDialogue(
                when (personal) {
                    0 -> if (counter.toFloat() % Utils.SettingData.AutoCollaspeCount.toFloat() == 0f || !Utils.SettingData.AutoCollaspe)
                             Dialogue(
                                 DialogueType.ImageStudent1,
                                 conversation.Profiles[profile].toProfile().Name,
                                 conversation.Profiles[profile].toProfile().Images[avator],
                                 null,
                                 image,
                                 overwriteImage,
                                 nameOverwrite
                             )
                         else
                             Dialogue(
                                 DialogueType.ImageStudent2,
                                 null,
                                 conversation.Profiles[profile].toProfile().Images[avator],
                                 null,
                                 image,
                                 overwriteImage,
                                 nameOverwrite
                             )
                    1 -> Dialogue(
                        DialogueType.ImageTeacher,
                        null,
                        null,
                        null,
                        image,
                        overwriteImage,
                        nameOverwrite
                    )
                    3 -> Dialogue(
                        DialogueType.ImageStudent2,
                        null,
                        conversation.Profiles[profile].toProfile().Images[avator],
                        null,
                        image,
                        overwriteImage,
                        nameOverwrite
                    )
                    else -> Dialogue.Empty
                }
            )

            if (personal == 0) imagecounter++
            else imagecounter = 0

            counter = 0

            avatorOverwrite = null
            nameOverwrite = null

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            val file = Utils.uriToFile(this@ConversationActivity, data.data!!)!!
            conversationOverwriteBindView.loadAvator(BitmapFactory.decodeFile(file.absolutePath))
            avatorOverwrite = file.absolutePath
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.save()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (multiSelect) {
            multiSelect = false
            dialogueAdapter.multiSelect = false
            dialogueAdapter.notifyDataSetChanged()
            return
        }
        super.onBackPressed()
    }

    fun addDialogue(dialogue : Dialogue) {
        dialogueAdapter.addDialogue(dialogue)
        if (dialogueAdapter.rewriteIndex != null) conversation.Dialogues[dialogueAdapter.rewriteIndex!!] = dialogue
        else if (dialogueAdapter.insertIndex != null) conversation.Dialogues.add(dialogueAdapter.insertIndex!!, dialogue)
        else conversation.Dialogues.add(dialogue)
        dialogueAdapter.rewriteIndex = null
        dialogueAdapter.insertIndex = null
    }

    fun removeDialogue(dialogue: Dialogue) {
        dialogueAdapter.removeDialogue(conversation.Dialogues.indexOf(dialogue))
        conversation.Dialogues.remove(dialogue)
    }

    inner class OnSendClick : OnClickListener {
        override fun onClick(v: View?) {
            val sender = findViewById<EditText>(R.id.sender)

            if (sender.text.toString() == "") {
                PopTip.show("请输入文本")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
                return
            }

            val name = conversation.Profiles[profile].toProfile().Name

            if (conversation.Dialogues.size > 0) {
                if (conversation.Dialogues[conversation.Dialogues.size - 1].Avator != conversation.Profiles[profile].toProfile().Images[avator]) counter = 0
            }

            val overwriteImage = if (avatorOverwrite == null) {
                null
            } else {
                val file = File(avatorOverwrite!!)
                Image(Utils.toMD5(file.name), file.absolutePath, true)
            }

            addDialogue(when (personal) {
                0 -> if (counter.toFloat() % Utils.SettingData.AutoCollaspeCount.toFloat() == 0f || !Utils.SettingData.AutoCollaspe || avatorOverwrite != null || nameOverwrite != null)
                    Dialogue(
                        DialogueType.Student1,
                        name,
                        conversation.Profiles[profile].toProfile().Images[avator],
                        listOf(sender.text.toString()),
                        null,
                        overwriteImage,
                        nameOverwrite
                    )
                else
                    Dialogue(
                        DialogueType.Student2,
                        null,
                        conversation.Profiles[profile].toProfile().Images[avator],
                        listOf(sender.text.toString()),
                        null,
                        overwriteImage,
                        nameOverwrite
                    )
                1 -> Dialogue(
                    DialogueType.Teacher,
                    null,
                    null,
                    listOf(sender.text.toString()),
                    null,
                    overwriteImage,
                    nameOverwrite
                )
                2 -> Dialogue(
                    DialogueType.Narrator,
                    null,
                    null,
                    listOf(sender.text.toString()),
                    null,
                    overwriteImage,
                    nameOverwrite
                )
                3 -> Dialogue(
                    DialogueType.Student2,
                    null,
                    conversation.Profiles[profile].toProfile().Images[avator],
                    listOf(sender.text.toString()),
                    null,
                    overwriteImage,
                    nameOverwrite
                )
                else -> Dialogue.Empty
            })

            if (personal == 0) counter++
            else counter = 0

            imagecounter = 0

            avatorOverwrite = null
            nameOverwrite = null

            sender.setText("")
        }
    }

    inner class OnKnotClick : OnClickListener {
        override fun onClick(v: View?) {
            addDialogue(
                Dialogue(
                    DialogueType.Knot,
                    conversation.Profiles[profile].toProfile().Name,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        }
    }

    inner class OnReplyClick : OnClickListener {
        override fun onClick(v: View?) {
            val bindView = ConversationAddReplyBindView(this@ConversationActivity)
            BottomMenu.show()
                .setMessage("回复列表")
                .setCustomView(bindView)
                .setOkButton("确认",
                    OnBottomMenuButtonClickListener { _, _ ->
                        val contents = bindView.getContents()
                        addDialogue(
                            Dialogue(
                                DialogueType.Reply,
                                null,
                                null,
                                contents,
                                null,
                                null,
                                null
                            )
                        )
                        false
                    })
                .setCancelButton("取消",
                    OnBottomMenuButtonClickListener { _, _ ->
                        false
                    })

            counter = 0

            imagecounter = 0
        }
    }

    inner class OnImageClick : OnClickListener {
        override fun onClick(v: View?) {
            if (personal == 2) {
                PopTip.show("旁白不可发送图片")
                    .setBackgroundColor(resources.getColor(R.color.warning))
                    .setMessageTextInfo(TextInfo().apply {
                        this.fontColor = resources.getColor(R.color.white)
                    })
                return
            }
            XXPermissions.with(this@ConversationActivity)
            .permission(Permission.READ_MEDIA_IMAGES)
            .request(object : OnPermissionCallback {

                override fun onGranted(
                    permissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    val intent = Intent(Intent.ACTION_PICK,  null)
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                    startActivityForResult(intent, 1)
                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        Toast.makeText(this@ConversationActivity, "请手动授予权限并重新上传图片", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@ConversationActivity, "请授予权限", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            counter = 0

            imagecounter = 0
        }
    }

    inner class OnPersonalClick : OnClickListener {
        override fun onClick(v: View?) {
            PopMenu.show(personalButton, if (Utils.SettingData.AutoCollaspe) listOf("学生", "老师", "旁白") else listOf("学生1", "学生2", "老师", "旁白") )
                .setWidth(Utils.dip2px(this@ConversationActivity, 80f))
                .setOverlayBaseView(false)
                .setAlignGravity(Gravity.TOP)
                .setOnMenuItemClickListener { _, _, index ->
                    personal = if (Utils.SettingData.AutoCollaspe) index else {
                        when (index) {
                            0 -> 0
                            1 -> 3
                            2 -> 1
                            3 -> 2
                            else -> throw IndexOutOfBoundsException()
                        }
                    }
                    when (personal) {
                        0 -> personalButton.setImageDrawable(resources.getDrawable(R.drawable.ic_student))
                        1 -> personalButton.setImageDrawable(resources.getDrawable(R.drawable.ic_teacher))
                        2 -> personalButton.setImageDrawable(resources.getDrawable(R.drawable.ic_narrator))
                        3 -> personalButton.setImageDrawable(resources.getDrawable(R.drawable.ic_student))
                    }
                    false
                }
        }
    }

    inner class OnProfileClick : OnClickListener {
        override fun onClick(v: View?) {
            PopMenu.show(personalButton, profileList)
                .setWidth(Utils.dip2px(this@ConversationActivity, 120f))
                .setOverlayBaseView(false)
                .setBaseView(v!!)
                .setAlignGravity(Gravity.TOP)
                .setOnMenuItemClickListener { _, _, index ->
                    profile = index
                    profileButton.setImageBitmap(profileBitmapList[index])
                    avatorList = conversation.Profiles[index].toProfile().Images.map { it.toBitmap() }
                    if (avatorList.size == 1) {
                        avatorButton.visibility = View.GONE
                    } else {
                        avatorButton.visibility = View.VISIBLE
                        avatorAdapter = AvatorAdapter(avatorList)
                    }
                    false
                }
        }
    }

    lateinit var avatorAdapter : AvatorAdapter

    inner class OnAvatorClick : OnClickListener {
        override fun onClick(v: View?) {
            BottomMenu.show()
                .setMessage("请选择一个头像，下滑关闭取消")
                .setCustomView(OnAvatorListBindView())
        }

        inner class OnAvatorListBindView : OnBindView<BottomDialog>(R.layout.fragment_avator_list) {
            override fun onBind(bottomDialog: BottomDialog?, v: View?) {
                val list = v!!.findViewById<RecyclerView>(R.id.avator_list)
                list.apply {
                    this.adapter = avatorAdapter
                    this.layoutManager = GridLayoutManager(this@ConversationActivity, 4)
                }
                dialog = bottomDialog
            }
        }
    }

    var dialog : BottomDialog? = null

    inner class OnAvatorItemClick(val index : Int) : OnClickListener {
        override fun onClick(v: View?) {
            avator = index
            dialog!!.dismiss()
            avatorButton.setImageBitmap(avatorList[index])
            dialog = null
        }
    }

    inner class OnOverwriteClick : OnClickListener {
        override fun onClick(v: View?) {
            BottomMenu.show()
                .setMessage("覆写对话头像/姓名")
                .setCustomView(conversationOverwriteBindView)
                .setDialogLifecycleCallback(object : DialogLifecycleCallback<BottomDialog>() {
                    override fun onDismiss(dialog: BottomDialog?) {
                        val name = conversationOverwriteBindView.view.findViewById<EditText>(R.id.overwrite_name)
                        nameOverwrite = if (name.text.toString() == "") null else name.text.toString()
                        super.onDismiss(dialog)
                    }
                })
            conversationOverwriteBindView.loadAvator(BitmapFactory.decodeFile(avatorOverwrite))
            conversationOverwriteBindView.loadName(nameOverwrite)
        }
    }

    inner class OnExportClick : OnClickListener {
        override fun onClick(v: View?) {
            val exportBindView = ConversationExportBindView(this@ConversationActivity, resources, conversation.Dialogues, dialogueAdapter)
            val dialog = CustomDialog.build()
            dialog.setCustomView(exportBindView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.setDialogLifecycleCallback(object : DialogLifecycleCallback<CustomDialog>() {
                override fun onDismiss(dialog: CustomDialog?) {
                    for (dialogue in dialogueAdapter.dialogues.toMutableList()) {
                        dialogueAdapter.removeDialogue(0)
                    }
                    for (dialogue in conversation.Dialogues) {
                        dialogueAdapter.addDialogue(dialogue)
                    }
                    super.onDismiss(dialog)
                }
            })
            dialog.show()
        }
    }

    inner class OnDialogueItemClick : DialogueListAdapter.OnItemClickListener {
        override fun OnItemClick(view: View?, data: Dialogue?) {
            if (multiSelect) {
                val check = view!!.findViewById<CheckBox>(R.id.check)
                check.isChecked = !check.isChecked
                return
            }
            PopMenu.show(view!!, listOf("删除", "重写", "插入").toTypedArray())
                .setOverlayBaseView(false)
                .setWidth(Utils.dip2px(this@ConversationActivity, 200f))
                .setAlignGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                .setOnMenuItemClickListener { dialog, _, index ->
                    when (index) {
                        0 -> removeDialogue(data!!)
                        1 -> {
                            dialogueAdapter.rewriteIndex = conversation.Dialogues.indexOf(data)
                            PopTip.show("发送对话以重写")
                                .setBackgroundColor(resources.getColor(R.color.hint))
                                .setMessageTextInfo(TextInfo().apply {
                                    this.fontColor = resources.getColor(R.color.white)
                                })
                        }
                        2 -> {
                            dialogueAdapter.insertIndex = conversation.Dialogues.indexOf(data)
                            PopTip.show("发送对话以插入")
                                .setBackgroundColor(resources.getColor(R.color.hint))
                                .setMessageTextInfo(TextInfo().apply {
                                    this.fontColor = resources.getColor(R.color.white)
                                })
                        }
                        else -> dialog.dismiss()
                    }
                    false
                }
        }
    }

    inner class OnExpandClick : OnClickListener {
        override fun onClick(v: View?) {
            extraExpanded = if (!extraExpanded) {
                (v as ImageButton).setImageDrawable(resources.getDrawable(R.drawable.animated_ic_dialogue_extra) as AnimatedVectorDrawable)
                val vectorDrawable = v.drawable as AnimatedVectorDrawable
                vectorDrawable.start()
                val layout = findViewById<LinearLayout>(R.id.basic_panel)
                val objectAnimation = ObjectAnimator.ofFloat(layout, "translationY", Utils.dip2px(this@ConversationActivity, 130f).toFloat(), Utils.dip2px(this@ConversationActivity, 0f).toFloat())
                objectAnimation.duration = 600
                objectAnimation.start()
                true
            } else {
                (v as ImageButton).setImageDrawable(resources.getDrawable(R.drawable.animated_ic_dialogue_extra_reverse) as AnimatedVectorDrawable)
                val vectorDrawable = v.drawable as AnimatedVectorDrawable
                vectorDrawable.start()
                val layout = findViewById<LinearLayout>(R.id.basic_panel)
                val objectAnimation = ObjectAnimator.ofFloat(layout, "translationY", Utils.dip2px(this@ConversationActivity, 0f).toFloat(), Utils.dip2px(this@ConversationActivity, 130f).toFloat())
                objectAnimation.duration = 600
                objectAnimation.start()
                false
            }
        }
    }

    inner class AvatorAdapter(val bitmaps : List<Bitmap>) : RecyclerView.Adapter<AvatorAdapter.AvatorViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatorViewHolder {
            val viewHolder = AvatorViewHolder(LayoutInflater.from(this@ConversationActivity).inflate(R.layout.avator_item, parent, false))
            viewHolder.avatorView = viewHolder.itemView.findViewById(R.id.avator)
            return  viewHolder
        }

        override fun getItemCount(): Int {
            return bitmaps.size
        }

        override fun onBindViewHolder(holder: AvatorViewHolder, position: Int) {
            val avator = holder.avatorView
            avator.setImageBitmap(bitmaps[position])
            avator.setOnClickListener(OnAvatorItemClick(position))
        }

        inner class AvatorViewHolder(itemView: View) : ViewHolder(itemView) {
            lateinit var avatorView : ImageButton
        }
    }
}
