package com.macro.macrotalkforandroid.ui.conversationlist

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
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils

class ConversationListAdapter(private val context: Context) :
    RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder>() {

    // 会话列表数据
    val conversationList : MutableList<Conversation> = Utils.storageData.Conversations

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.conversation_item, parent, false)
        return ConversationViewHolder(itemView)
    }

    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val data: Conversation = conversationList[position]
        holder.conversationListImage.setImageBitmap(BitmapFactory.decodeFile(data.Image.ImageOriginalUri))
        holder.conversationListTitle.text = data.Title
        holder.conversationListLastDialogue.text = data.LastDialogue
    }

    // 获取列表项数量
    override fun getItemCount(): Int {
        return conversationList.size
    }

    // 在 ViewHolder 添加到窗口时执行动画
    override fun onViewAttachedToWindow(holder: ConversationViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    // 添加新的会话项
    fun addItem(conversation: Conversation) {
        conversationList.add(conversation)
        notifyItemInserted(conversationList.size - 1)
    }

    // 移除会话项
    fun removeItem(index : Int) {
        conversationList.removeAt(index)
        notifyItemRemoved(index)
    }

    // 替换会话项
    fun replaceItem(index : Int, newConversation : Conversation) {
        conversationList[index] = newConversation
        notifyItemChanged(index)
    }

    // 内部 ViewHolder 类
    inner class ConversationViewHolder(itemView: View) : ViewHolder(itemView) {
        val conversationListImage: ImageView
        val conversationListTitle: TextView
        val conversationListLastDialogue: TextView

        val view : View

        init {
            // 初始化视图
            conversationListImage = itemView.findViewById<View>(R.id.conversationlist_Image) as ImageView
            conversationListTitle = itemView.findViewById<View>(R.id.conversationlist_Title) as TextView
            conversationListLastDialogue = itemView.findViewById<View>(R.id.conversationlist_LastDialogue) as TextView

            // 设置点击和长按监听器
            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, layoutPosition)
                }
            }

            itemView.setOnLongClickListener { v ->
                if (onItemLongClickListener != null) {
                    onItemLongClickListener!!.OnItemLongClick(v, layoutPosition)
                }
                true
            }

            view = itemView
        }
    }

    // 点击监听器接口
    interface OnItemClickListener {
        fun OnItemClick(view: View?, index: Int)
    }

    // 长按监听器接口
    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, index: Int)
    }

    // 设置点击监听器
    private var onItemClickListener: OnItemClickListener? = null

    // 设置长按监听器
    private var onItemLongClickListener: OnItemLongClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}
