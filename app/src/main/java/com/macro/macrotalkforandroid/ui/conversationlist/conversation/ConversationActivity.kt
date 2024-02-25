package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import com.kongzue.dialogx.util.TextInfo
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.DialogueType
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils


class ConversationActivity : AppCompatActivity() {
    companion object {
        lateinit var conversation : Conversation
    }

    var extraExpanded : Boolean = false

    lateinit var avatorAdapter : ConversationAvatorAdapter

    lateinit var dialogueAdapter : DialogueListAdapter

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
            avatorAdapter = ConversationAvatorAdapter(conversation.Profiles[0].toProfile().Images)
            avatorAdapter.context = this@ConversationActivity
            avatorAdapter.setOnItemClickListener(OnAvatorViewClick(conversation.Profiles[0].toProfile().Images.size - 1))
            avator.apply {
                this.orientation = ViewPager2.ORIENTATION_VERTICAL
                this.adapter = avatorAdapter
            }
        }

        val imageButton = findViewById<ImageButton>(R.id.add_conversation_cover)
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

        dialogueAdapter = DialogueListAdapter(this@ConversationActivity, listOf<Dialogue>().toMutableList(), resources)
        val layoutManager = LinearLayoutManager(this@ConversationActivity, RecyclerView.VERTICAL, false)
        list.apply {
            this.adapter = dialogueAdapter
            this.layoutManager = layoutManager
        }
    }

    fun addDialogue(dialogue : Dialogue) {
        conversation.Dialogues.add(dialogue)
        dialogueAdapter.addDialogue(dialogue)
    }

    var counter = 0

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

            addDialogue(when (personal.currentItem) {
                0 -> if (counter.toFloat() % Utils.SettingData.AutoCollaspeCount.toFloat() == 0f)
                        Dialogue(
                            DialogueType.Student1,
                            name,
                            conversation.Profiles[profile.currentItem].toProfile().Images[avator.currentItem],
                            listOf(sender.text.toString()).toTypedArray(),
                            null)
                    else
                        Dialogue(
                            DialogueType.Student2,
                            null,
                            null,
                            listOf(sender.text.toString()).toTypedArray(),
                            null)
                1 -> Dialogue(
                        DialogueType.Teacher,
                        null,
                        null,
                        listOf(sender.text.toString()).toTypedArray(),
                    null)
                2 -> Dialogue(
                        DialogueType.Narrator,
                        null,
                        null,
                        listOf(sender.text.toString()).toTypedArray(),
                    null)
                3 -> Dialogue(
                        DialogueType.Student2,
                        null,
                        null,
                        listOf(sender.text.toString()).toTypedArray(),
                        null)
                else -> Dialogue.Empty
            })

            counter++
        }
    }

    inner class OnKnotClick : OnClickListener {
        override fun onClick(v: View?) {
            addDialogue(
                Dialogue(
                    DialogueType.Knot,
                    null,
                    null,
                    null,
                    null)
            )
        }
    }

    inner class OnReplyClick : OnClickListener {
        override fun onClick(v: View?) {
            BottomMenu.show()
                .setMessage("对话列表")
                .setCustomView(ConversationAddReplyBindView())
                .setOkButton("确认",
                    OnBottomMenuButtonClickListener { dialog, v ->

                        false
                    })
                .setCancelButton("取消",
                    OnBottomMenuButtonClickListener { dialog, v ->

                        false
                    })

        }
    }

    inner class OnImageClick : OnClickListener {
        override fun onClick(v: View?) {

        }
    }

    inner class OnOverwriteClick : OnClickListener {
        override fun onClick(v: View?) {

        }
    }

    inner class OnProfileViewChangeCallback : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val avator = findViewById<ViewPager2>(R.id.conversation_avator)
            if (conversation.Profiles[position].toProfile().Images.size == 1) {
                val animation = AlphaAnimation(1f, 0f)
                animation.duration = 600
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        avator.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                avator.startAnimation(animation)
            } else {
                val animation = AlphaAnimation(0f, 1f)
                animation.duration = 600
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        avator.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                avator.startAnimation(animation)
            }
            avatorAdapter.images = conversation.Profiles[position].toProfile().Images
            avatorAdapter.notifyDataSetChanged()
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
            if (data == size) {
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