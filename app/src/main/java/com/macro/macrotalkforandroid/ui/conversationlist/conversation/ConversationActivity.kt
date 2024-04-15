package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import com.kongzue.dialogx.util.TextInfo
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.DialogueType
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class ConversationActivity : AppCompatActivity() {
    companion object {
        lateinit var conversation : Conversation
    }

    var extraExpanded : Boolean = false

    lateinit var avatorAdapter : ConversationAvatorAdapter

    lateinit var dialogueAdapter : DialogueListAdapter

    lateinit var conversationOverwriteBindView: ConversationOverwriteBindView

    var counter = 0

    var imagecounter = 0

    var avatorOverwrite : String? = null

    var nameOverwrite : String? = null

    var multiSelect : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        title = "对话 - " + conversation.Title

        val list = findViewById<RecyclerView>(R.id.Conversation_DialogueList)
        list.setBackgroundColor(Color.parseColor(Utils.SettingData.ConversationBgColor))

        val personal = findViewById<ViewPager2>(R.id.conversation_personal)
        val personalAdapter = ConversationPersonalAdapter()
        personalAdapter.context = this@ConversationActivity
        personalAdapter.setOnItemClickListener(OnPersonalViewClick())
        personal.apply {
            this.orientation = ViewPager2.ORIENTATION_VERTICAL
            this.adapter = personalAdapter
        }

        val profile = findViewById<ViewPager2>(R.id.conversation_profile)
        if (conversation.Profiles.size == 1) {
            profile.visibility = View.GONE
        } else {
            profile.registerOnPageChangeCallback(OnProfileViewChangeCallback())
            val profileAdapter = ConversationProfileAdapter(conversation.Profiles.map { it.toProfile().Images[0] })
            profileAdapter.context = this@ConversationActivity
            profileAdapter.setOnItemClickListener(OnProfileViewClick(conversation.Profiles.size - 1))
            profile.apply {
                this.orientation = ViewPager2.ORIENTATION_VERTICAL
                this.adapter = profileAdapter
            }
        }

        val avator = findViewById<ViewPager2>(R.id.conversation_avator)
        if (conversation.Profiles.size == 1 && conversation.Profiles[0].toProfile().Images.size == 1) {
            avator.visibility = View.GONE
        } else {
            if (conversation.Profiles[0].toProfile().Images.size == 1) {
                avator.visibility = View.INVISIBLE
            }
        }
        avatorAdapter = ConversationAvatorAdapter(conversation.Profiles[0].toProfile().Images)
        avatorAdapter.context = this@ConversationActivity
        avatorAdapter.setOnItemClickListener(OnAvatorViewClick(conversation.Profiles[0].toProfile().Images.size - 1))
        avator.apply {
            this.orientation = ViewPager2.ORIENTATION_VERTICAL
            this.adapter = avatorAdapter
        }

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val file = Utils.uriToFile(this@ConversationActivity, data.data!!)!!

            val image = Image(Utils.toMD5(file.name), file.absolutePath, true)

            val personal = findViewById<ViewPager2>(R.id.conversation_personal)
            val profile = findViewById<ViewPager2>(R.id.conversation_profile)
            val avator = findViewById<ViewPager2>(R.id.conversation_avator)

            if (conversation.Dialogues.size > 0) {
                if (conversation.Dialogues[conversation.Dialogues.size - 1].Avator != conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem]) imagecounter = 0
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
                when (personal.currentItem) {
                    0 -> if (counter.toFloat() % Utils.SettingData.AutoCollaspeCount.toFloat() == 0f || !Utils.SettingData.AutoCollaspe)
                             Dialogue(
                                 DialogueType.ImageStudent1,
                                 conversation.Profiles[profile.currentItem].toProfile().Name,
                                 conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
                                 null,
                                 image,
                                 overwriteImage,
                                 nameOverwrite
                             )
                         else
                             Dialogue(
                                 DialogueType.ImageStudent2,
                                 null,
                                 conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
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
                        conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
                        null,
                        image,
                        overwriteImage,
                        nameOverwrite
                    )
                    else -> Dialogue.Empty
                }
            )

            if (personal.currentItem == 0) imagecounter++
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
        conversation.Dialogues.add(dialogue)
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

            val personal = findViewById<ViewPager2>(R.id.conversation_personal)
            val profile = findViewById<ViewPager2>(R.id.conversation_profile)
            val avator = findViewById<ViewPager2>(R.id.conversation_avator)

            val name = conversation.Profiles[profile.currentItem].toProfile().Name

            if (conversation.Dialogues.size > 0) {
                if (conversation.Dialogues[conversation.Dialogues.size - 1].Avator != conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem]) counter = 0
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

            addDialogue(when (personal.currentItem) {
                0 -> if (counter.toFloat() % Utils.SettingData.AutoCollaspeCount.toFloat() == 0f || !Utils.SettingData.AutoCollaspe || avatorOverwrite != null || nameOverwrite != null)
                    Dialogue(
                        DialogueType.Student1,
                        name,
                        conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
                        listOf(sender.text.toString()),
                        null,
                        overwriteImage,
                        nameOverwrite
                    )
                else
                    Dialogue(
                        DialogueType.Student2,
                        null,
                        conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
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
                    conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
                    listOf(sender.text.toString()),
                    null,
                    overwriteImage,
                    nameOverwrite
                )
                else -> Dialogue.Empty
            })

            if (personal.currentItem == 0) counter++
            else counter = 0

            imagecounter = 0

            avatorOverwrite = null
            nameOverwrite = null

            sender.setText("")
        }
    }

    inner class OnKnotClick : OnClickListener {
        override fun onClick(v: View?) {
            val profile = findViewById<ViewPager2>(R.id.conversation_profile)
            addDialogue(
                Dialogue(
                    DialogueType.Knot,
                    conversation.Profiles[profile.currentItem].toProfile().Name,
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
            return
            val exportBindView = ConversationExportBindView(this@ConversationActivity, resources, conversation.Dialogues, dialogueAdapter)
            val dialog = CustomDialog.build()
            dialog.setCustomView(exportBindView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }
    }

    inner class OnProfileViewChangeCallback : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val avator = findViewById<ViewPager2>(R.id.conversation_avator)
            if (conversation.Profiles[position].toProfile().Images.size == 1) {
                if (avator.alpha == 1f) {
                    val animation = AlphaAnimation(1f, 0f)
                    animation.duration = 600
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            avator.alpha = 0f
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            avator.visibility = View.INVISIBLE
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }

                    })
                    avator.startAnimation(animation)
                }
            } else {
                if (avator.alpha == 0f) {
                    val animation = AlphaAnimation(0f, 1f)
                    animation.duration = 600
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            avator.alpha = 1f
                            avator.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    avator.startAnimation(animation)
                }
            }
            avatorAdapter.images = conversation.Profiles[position].toProfile().Images
            avatorAdapter.notifyDataSetChanged()
            avatorAdapter.setOnItemClickListener(OnAvatorViewClick(avatorAdapter.images.size))
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

    interface OnItemClickListener {
        fun OnItemClick(view : View?, data : Int)
    }

    inner class OnPersonalViewClick(): OnItemClickListener {
        override fun OnItemClick(view: View?, data: Int) {
            val personal = findViewById<ViewPager2>(R.id.conversation_personal)
            if (data == 2) {
                personal.setCurrentItem(0, true)
            } else {
                personal.setCurrentItem(data + 1, true)
            }
        }
    }

    inner class OnProfileViewClick(val size : Int): OnItemClickListener {
        override fun OnItemClick(view: View?, data: Int) {
            val profile = findViewById<ViewPager2>(R.id.conversation_profile)
            if (data == size) {
                profile.setCurrentItem(0, true)
            } else {
                profile.setCurrentItem(data + 1, true)
            }
        }
    }

    inner class OnAvatorViewClick(val size : Int): OnItemClickListener {
        override fun OnItemClick(view: View?, data: Int) {
            val avator = findViewById<ViewPager2>(R.id.conversation_avator)
            if (data == size - 1) {
                avator.setCurrentItem(0, true)
            } else {
                avator.setCurrentItem(data + 1, true)
            }
        }
    }

    inner class OnExpandClick : OnClickListener {
        override fun onClick(v: View?) {
            extraExpanded = if (!extraExpanded) {
                (v as ImageButton).setImageDrawable(resources.getDrawable(R.drawable.animated_ic_dialogue_extra) as AnimatedVectorDrawable)
                val vectorDrawable = v.drawable as AnimatedVectorDrawable
                vectorDrawable.start()
                val layout = findViewById<LinearLayout>(R.id.conversation_extra_layout)
                val animation = AlphaAnimation(0f, 1f)
                animation.duration = 600
                animation.setAnimationListener(object : AnimationListener{
                    override fun onAnimationStart(animation: Animation?) {
                        layout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
                layout.startAnimation(animation)
                true
            } else {
                (v as ImageButton).setImageDrawable(resources.getDrawable(R.drawable.animated_ic_dialogue_extra_reverse) as AnimatedVectorDrawable)
                val vectorDrawable = v.drawable as AnimatedVectorDrawable
                vectorDrawable.start()
                val layout = findViewById<LinearLayout>(R.id.conversation_extra_layout)
                val animation = AlphaAnimation(1f, 0f)
                animation.duration = 600
                animation.setAnimationListener(object : AnimationListener{
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        layout.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
                layout.startAnimation(animation)
                false
            }
        }
    }
}