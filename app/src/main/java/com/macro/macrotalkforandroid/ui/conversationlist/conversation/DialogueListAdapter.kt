package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Dialogue
import com.macro.macrotalkforandroid.DialogueType
import com.macro.macrotalkforandroid.R


class DialogueListAdapter(private val context : Context, val dialogues : MutableList<Dialogue>, val resources : Resources) :
    RecyclerView.Adapter<DialogueListAdapter.DialogueViewHodler>() {
    //重写索引
    var rewriteIndex : Int? = null
    //插入索引
    var insertIndex : Int? = null
    //多选模式
    var multiSelect : Boolean = false

    // 根据对话类型返回对应的布局类型
    override fun getItemViewType(position: Int): Int {
        return if (dialogues[position] != Dialogue.Empty) {
            dialogues[position].Type!!.ordinal
        } else {
            -1
        }
    }
    // 创建对话ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogueViewHodler {
        val itemView = when (viewType) {
            0 -> LayoutInflater.from(context).inflate(R.layout.dialogue_student1, parent, false)
            1 -> LayoutInflater.from(context).inflate(R.layout.dialogue_student2, parent, false)
            2 -> LayoutInflater.from(context).inflate(R.layout.dialogue_teacher, parent, false)
            3 -> LayoutInflater.from(context).inflate(R.layout.dialogue_narrator, parent, false)
            4 -> LayoutInflater.from(context).inflate(R.layout.dialogue_knot, parent, false)
            5 -> LayoutInflater.from(context).inflate(R.layout.dialogue_reply, parent, false)
            6 -> LayoutInflater.from(context).inflate(R.layout.dialogue_student1_image, parent, false)
            7 -> LayoutInflater.from(context).inflate(R.layout.dialogue_student2_image, parent, false)
            8 -> LayoutInflater.from(context).inflate(R.layout.dialogue_teacher_image, parent, false)
            else -> throw IllegalArgumentException()
        }
        return DialogueViewHodler(itemView)
    }
    // 获取对话列表的大小
    override fun getItemCount(): Int {
        return dialogues.size
    }
    // 绑定对话数据到ViewHolder
    override fun onBindViewHolder(holder: DialogueViewHodler, position: Int) {
        holder.BindDialogue(dialogues[position])
    }
    // 添加对话
    fun addDialogue(dialogue : Dialogue) {
        rewriteIndex?.let {
            changeDialogue(dialogue, it)
            return
        }
        insertIndex?.let {
            insertDialogue(dialogue, it)
            return
        }
        dialogues.add(dialogue)
        notifyItemInserted(dialogues.size - 1)
    }
    // 插入对话
    fun insertDialogue(dialogue : Dialogue, insertIndex : Int) {
        dialogues.add(insertIndex, dialogue)
        notifyItemInserted(insertIndex)
    }
    // 移除对话
    fun removeDialogue(index : Int) {
        dialogues.removeAt(index)
        notifyItemRemoved(index)
    }
    // 更改对话
    fun changeDialogue(newDialogue : Dialogue, index : Int) {
        dialogues[index] = newDialogue
        notifyItemChanged(index)
    }
    // 对话ViewHolder
    inner class DialogueViewHodler(val view : View) : RecyclerView.ViewHolder(view) {
        // 绑定对话数据
        fun BindDialogue(dialogue : Dialogue) {
            val avatorView : ImageView
            val nameView : TextView
            val contentView : TextView
            val contentImageView : ImageView
            val contentsView : RecyclerView
            when (dialogue.Type) {
                DialogueType.Student1 -> {
                    // 学生1类型对话
                    avatorView = view.findViewById(R.id.Dialogue_Student1_Avator)
                    // 设置学生1头像
                    avatorView.setImageBitmap(if (!dialogue.Avator!!.isNotPrefab) {
                        BitmapFactory.decodeStream(context.assets.open(dialogue.Avator.ImageName + ".jpg"))
                    } else {
                        BitmapFactory.decodeFile(dialogue.Avator.ImageOriginalUri)
                    })
                    // 如果有覆盖头像，则更新头像
                    dialogue.AvatorOverwrite?.let {
                        avatorView.setImageBitmap(BitmapFactory.decodeFile(dialogue.Avator.ImageOriginalUri))
                    }
                    // 设置学生1姓名
                    nameView = view.findViewById(R.id.Dialogue_Student1_Name)
                    nameView.text = dialogue.Name
                    // 如果有覆盖姓名，则更新姓名
                    dialogue.NameOverwrite?.let {
                        nameView.text = dialogue.NameOverwrite
                    }
                    // 设置学生1对话内容
                    contentView = view.findViewById(R.id.Dialogue_Student1_Content)
                    contentView.text = dialogue.Content!![0]
                }
                // 学生2类型对话
                DialogueType.Student2 -> {
                    contentView = view.findViewById(R.id.Dialogue_Student2_Content)
                    contentView.text = dialogue.Content!![0]
                }
                //sensei类型对话
                DialogueType.Teacher -> {
                    contentView = view.findViewById(R.id.Dialogue_Teacher_Content)
                    contentView.text = dialogue.Content!![0]
                }
                //旁白类型对话
                DialogueType.Narrator -> {
                    contentView = view.findViewById(R.id.Dialogue_Narrator_Content)
                    contentView.text = dialogue.Content!![0]
                }
                //羁绊类型对话
                DialogueType.Knot -> {
                    contentView = view.findViewById(R.id.Dialogue_Knot_Content)
                    contentView.text = resources.getString(R.string.knot_content, dialogue.Name)
                }
                // 回复类型对话
                DialogueType.Reply -> {
                    contentsView = view.findViewById(R.id.Dialogue_Reply_Contents)
                    val adapter = ReplyListAdapter(dialogue.Content!!)
                    // 回复内容列表适配器
                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    contentsView.apply {
                        this.adapter = adapter
                        this.layoutManager = layoutManager
                    }
                }
                // 学生1图片类型对话
                DialogueType.ImageStudent1 -> {
                    avatorView = view.findViewById(R.id.Dialogue_Student1_Image_Avator)
                    // 设置学生1图片类型对话头像
                    avatorView.setImageBitmap(if (!dialogue.Avator!!.isNotPrefab) {
                        BitmapFactory.decodeStream(context.assets.open(dialogue.Avator.ImageName + ".jpg"))
                    } else {
                        BitmapFactory.decodeFile(dialogue.Avator.ImageOriginalUri)
                    })
                    // 如果有覆盖头像，则更新头像
                    dialogue.AvatorOverwrite?.let {
                        avatorView.setImageBitmap(BitmapFactory.decodeFile(dialogue.Avator.ImageOriginalUri))
                    }
                    // 设置学生1图片类型对话姓名
                    nameView = view.findViewById(R.id.Dialogue_Student1_Image_Name)
                    nameView.text = dialogue.Name
                    // 如果有覆盖姓名，则更新姓名
                    dialogue.NameOverwrite?.let {
                        nameView.text = dialogue.NameOverwrite
                    }
                    // 设置学生1图片类型对话内容图片
                    contentImageView = view.findViewById(R.id.Dialogue_Student1_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }
                DialogueType.ImageStudent2 -> {
                    // 学生2图片类型对话
                    // 设置学生2图片类型对话内容图片
                    contentImageView = view.findViewById(R.id.Dialogue_Student2_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }
                DialogueType.ImageTeacher -> {
                    // sensei图片类型对话
                    contentImageView = view.findViewById(R.id.Dialogue_Teacher_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }// 设置内容图片
                null -> throw IllegalArgumentException()
            }
            // 设置对话项点击监听器
            view.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, dialogues[layoutPosition])
                }
            }
            // 设置对话项长按监听器
            view.setOnLongClickListener { v ->
                if (onItemLongClickListener != null) {
                    onItemLongClickListener!!.OnItemLongClick(v, dialogues[layoutPosition])
                }
                true
            }
            // 设置多选模式下的选择框可见性
            val check = view.findViewById<CheckBox>(R.id.check)
            if (multiSelect) {
                check.visibility = View.VISIBLE
            } else {
                check.visibility = View.GONE
            }
        }
        // 回复列表适配器
        inner class ReplyListAdapter(val replyList : List<String>) : RecyclerView.Adapter<ReplyListAdapter.ReplyItemViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyItemViewHolder {
                val itemView = LayoutInflater.from(context).inflate(R.layout.dialogue_reply_item, parent, false)
                return ReplyItemViewHolder(itemView)
            }

            override fun getItemCount(): Int {
                return replyList.size
            }

            override fun onBindViewHolder(holder: ReplyItemViewHolder, position: Int) {
                holder.content.text = replyList[position]
            }

            inner class ReplyItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
                val content : TextView = itemView.findViewById(R.id.Dialogue_Reply_Item_Content)
            }
        }
    }
    // 对话项点击监听器接口
    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Dialogue?)
    }
    // 对话项长按监听器接口
    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, data : Dialogue?)
    }

    private var onItemClickListener : OnItemClickListener? = null

    private var onItemLongClickListener : OnItemLongClickListener? = null
    // 设置对话项点击监听器
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
    // 设置对话项长按监听器
    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}