package com.Macro.macrotalkforandroid

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Macro.macrotalkforandroid.R

class DialogueListAdapter(private val context : Context, val dialogues : MutableList<Dialogue>, val resources : Resources) :
    RecyclerView.Adapter<DialogueListAdapter.DialogueViewHodler>() {

    override fun getItemViewType(position: Int): Int {
        return  dialogues[position].Type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogueViewHodler {
        val itemView = when (viewType) {
            0 -> View.inflate(context, R.layout.dialogue_student1, null)
            1 -> View.inflate(context, R.layout.dialogue_student2, null)
            2 -> View.inflate(context, R.layout.dialogue_teacher, null)
            3 -> View.inflate(context, R.layout.dialogue_narrator, null)
            4 -> View.inflate(context, R.layout.dialogue_knot, null)
            5 -> View.inflate(context, R.layout.dialogue_reply, null)
            6 -> View.inflate(context, R.layout.dialogue_student1_image, null)
            7 -> View.inflate(context, R.layout.dialogue_student2_image, null)
            8 -> View.inflate(context, R.layout.dialogue_teacher_image, null)
            else -> throw IllegalArgumentException()
        }
        return DialogueViewHodler(itemView)
    }

    override fun getItemCount(): Int {
        return dialogues.size
    }

    override fun onBindViewHolder(holder: DialogueViewHodler, position: Int) {
        holder.BindDialogue(dialogues[position])
    }

    fun addDialogue(dialogue : Dialogue) {
        dialogues.add(dialogue)
        notifyItemInserted(dialogues.size)
    }

    fun insertDialogue(dialogue : Dialogue, insertIndex : Int) {
        dialogues.add(insertIndex, dialogue)
        notifyItemInserted(insertIndex)
    }

    fun removeDialogue(index : Int) {
        dialogues.removeAt(index)
        notifyItemRemoved(index)
    }

    fun changeDialogue(newDialogue : Dialogue, index : Int) {
        dialogues[index] = newDialogue
        notifyItemChanged(index)
    }

    inner class DialogueViewHodler(val view : View) : RecyclerView.ViewHolder(view) {
        fun BindDialogue(dialogue : Dialogue) {
            val avatorView : ImageView
            val nameView : TextView
            val contentView : TextView
            val contentImageView : ImageView
            val contentsView : RecyclerView
            when (dialogue.Type) {
                DialogueType.Student1 -> {
                    avatorView = view.findViewById(R.id.Dialogue_Student1_Avator)
                    avatorView.setImageBitmap(BitmapFactory.decodeFile(dialogue.Avator!!.ImageOriginalUri))
                    nameView = view.findViewById(R.id.Dialogue_Student1_Name)
                    nameView.text = dialogue.Name
                    contentView = view.findViewById(R.id.Dialogue_Student1_Content)
                    contentView.text = dialogue.Content!![0]
                }
                DialogueType.Student2 -> {
                    contentView = view.findViewById(R.id.Dialogue_Student2_Content)
                    contentView.text = dialogue.Content!![0]
                }
                DialogueType.Teacher -> {
                    contentView = view.findViewById(R.id.Dialogue_Teacher_Content)
                    contentView.text = dialogue.Content!![0]
                }
                DialogueType.Narrator -> {
                    contentView = view.findViewById(R.id.Dialogue_Narrator_Content)
                    contentView.text = dialogue.Content!![0]
                }
                DialogueType.Knot -> {
                    contentView = view.findViewById(R.id.Dialogue_Knot_Content)
                    contentView.text = resources.getString(R.string.knot_content, dialogue.Name)
                }
                DialogueType.Reply -> {
                    contentsView = view.findViewById(R.id.Dialogue_Reply_Contents)
                    val adapter = ReplyListAdapter(dialogue.Content!!.toList())
                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    contentsView.apply {
                        this.adapter = adapter
                        this.layoutManager = layoutManager
                    }
                }
                DialogueType.ImageStudent1 -> {
                    avatorView = view.findViewById(R.id.Dialogue_Student1_Image_Avator)
                    avatorView.setImageBitmap(BitmapFactory.decodeFile(dialogue.Avator!!.ImageOriginalUri))
                    nameView = view.findViewById(R.id.Dialogue_Student1_Image_Name)
                    nameView.text = dialogue.Name
                    contentImageView = view.findViewById(R.id.Dialogue_Student1_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }
                DialogueType.ImageStudent2 -> {
                    contentImageView = view.findViewById(R.id.Dialogue_Student2_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }
                DialogueType.ImageTeacher -> {
                    contentImageView = view.findViewById(R.id.Dialogue_Teacher_Image_Content)
                    contentImageView.setImageBitmap(BitmapFactory.decodeFile(dialogue.ImageContent!!.ImageOriginalUri))
                }
            }

            view.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, dialogues[layoutPosition])
                }
            }

            view.setOnLongClickListener { v ->
                if (onItemLongClickListener != null) {
                    onItemLongClickListener!!.OnItemLongClick(v, dialogues[layoutPosition])
                }
                true
            }
        }

        inner class ReplyListAdapter(val replyList : List<String>) : RecyclerView.Adapter<ReplyListAdapter.ReplyItemViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyItemViewHolder {
                val itemView = View.inflate(context, R.layout.dialogue_reply_item, null)
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

    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Dialogue?)
    }

    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, data : Dialogue?)
    }

    private var onItemClickListener : OnItemClickListener? = null

    private var onItemLongClickListener : OnItemLongClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}