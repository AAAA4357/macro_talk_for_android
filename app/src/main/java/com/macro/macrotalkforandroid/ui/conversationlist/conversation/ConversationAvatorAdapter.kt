package com.macro.macrotalkforandroid.ui.conversationlist.conversation

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.R

class ConversationAvatorAdapter(var images : List<Image>)
    : RecyclerView.Adapter<ConversationAvatorAdapter.ConversationAvatorViewHolder>() {

    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationAvatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.conversation_viewpager_item,parent,false)
        return ConversationAvatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationAvatorAdapter.ConversationAvatorViewHolder, position: Int) {
        holder.itemImage.setImageBitmap(
            if (!images[position].isNotPrefab) {
                BitmapFactory.decodeStream(context.assets.open(images[position].ImageName + ".jpg"))
            } else {
                BitmapFactory.decodeFile(images[position].ImageOriginalUri)
            }
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ConversationAvatorViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val itemImage : ImageView

        init {
            itemImage = itemView.findViewById(R.id.item_image)

            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, layoutPosition)
                }
            }
        }
    }

    private var onItemClickListener : ConversationActivity.OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: ConversationActivity.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}