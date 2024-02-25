package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.ui.profilelist.ProfileListAdapter

class ConversationPersonalAdapter()
    : RecyclerView.Adapter<ConversationPersonalAdapter.ConversationPersonalViewHolder>() {

    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationPersonalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.conversation_viewpager_item,parent,false)
        return ConversationPersonalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ConversationPersonalViewHolder, position: Int) {
        holder.bindImage(position)
    }

    inner class ConversationPersonalViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val itemImage : ImageView

        init {
            itemImage = itemView.findViewById(R.id.item_image)

            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, layoutPosition)
                }
            }
        }

        fun bindImage(index : Int) {
            itemImage.setImageDrawable(
                when (index) {
                0 -> context.getDrawable(R.drawable.ic_student)
                1 -> context.getDrawable(R.drawable.ic_teacher)
                2 -> context.getDrawable(R.drawable.ic_narrator)
                else -> null
                }
            )
        }
    }

    private var onItemClickListener : ConversationActivity.OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener : ConversationActivity.OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
}