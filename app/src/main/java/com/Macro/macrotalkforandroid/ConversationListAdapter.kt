package com.Macro.macrotalkforandroid

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ConversationListAdapter(private val context: Context, val conversationList: MutableList<Conversation>) :
    RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.conversation_item, parent, false)
        return ConversationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val data: Conversation = conversationList[position]
        holder.conversationListImage.setImageBitmap(BitmapFactory.decodeFile(data.Image.ImageOriginalUri))
        holder.conversationListTitle.text = data.Title
        holder.conversationListLastDialogue.text = data.LastDialogue
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onViewAttachedToWindow(holder: ConversationViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    fun addItem(conversation: Conversation) {
        conversationList.add(conversation)
        notifyItemInserted(conversationList.size)
    }

    fun removeItem(index : Int) {
        conversationList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun replaceItem(index : Int, newConversation : Conversation) {
        conversationList[index] = newConversation
        notifyItemChanged(index)
    }

    inner class ConversationViewHolder(itemView: View) : ViewHolder(itemView) {
        val conversationListImage: ImageView
        val conversationListTitle: TextView
        val conversationListLastDialogue: TextView

        val view : View

        init {
            conversationListImage = itemView.findViewById<View>(R.id.conversationlist_Image) as ImageView
            conversationListTitle = itemView.findViewById<View>(R.id.conversationlist_Title) as TextView
            conversationListLastDialogue = itemView.findViewById<View>(R.id.conversationlist_LastDialogue) as TextView

            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, conversationList[layoutPosition])
                }
            }

            itemView.setOnLongClickListener { v ->
                if (onItemLongClickListener != null) {
                    onItemLongClickListener!!.OnItemLongClick(v, conversationList[layoutPosition])
                }
                true
            }

            view = itemView
        }
    }

    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Conversation?)
    }

    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, data: Conversation?)
    }

    private var onItemClickListener: OnItemClickListener? = null

    private var onItemLongClickListener: OnItemLongClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}