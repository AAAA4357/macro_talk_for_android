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

// 个人对话适配器，用于显示个人对话的图片
class ConversationPersonalAdapter()
    : RecyclerView.Adapter<ConversationPersonalAdapter.ConversationPersonalViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationPersonalViewHolder {
        // 加载布局文件
        val view = LayoutInflater.from(parent.context).inflate(R.layout.conversation_viewpager_item, parent, false)
        return ConversationPersonalViewHolder(view)
    }

    override fun getItemCount(): Int {
        // 返回项数为 3
        return 3
    }

    override fun onBindViewHolder(holder: ConversationPersonalViewHolder, position: Int) {
        // 绑定图片
        holder.bindImage(position)
    }

    inner class ConversationPersonalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView

        init {
            // 初始化控件
            itemImage = itemView.findViewById(R.id.item_image)

            // 设置点击事件
            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, layoutPosition)
                }
            }
        }

        // 绑定图片
        fun bindImage(index: Int) {
            itemImage.setImageDrawable(
                when (index) {
                    // 根据索引设置不同的图片
                    0 -> context.getDrawable(R.drawable.ic_student)
                    1 -> context.getDrawable(R.drawable.ic_teacher)
                    2 -> context.getDrawable(R.drawable.ic_narrator)
                    else -> null
                }
            )
        }
    }

    // 点击事件监听器
    private var onItemClickListener: ConversationActivity.OnItemClickListener? = null

    // 设置点击事件监听器
    fun setOnItemClickListener(onItemClickListener: ConversationActivity.OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
}
