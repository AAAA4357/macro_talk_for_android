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

// 对话档案适配器，用于显示对话档案的图片
class ConversationProfileAdapter(val images: List<Image>) :
    RecyclerView.Adapter<ConversationProfileAdapter.ConversationProfileViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationProfileViewHolder {
        // 加载布局文件
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation_viewpager_item, parent, false)
        return ConversationProfileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ConversationProfileViewHolder, position: Int) {
        // 设置图片
        holder.itemImage.setImageBitmap(
            if (!images[position].isNotPrefab) {
                BitmapFactory.decodeStream(context.assets.open(images[position].ImageName + ".jpg"))
            } else {
                BitmapFactory.decodeFile(images[position].ImageOriginalUri)
            }
        )
    }

    inner class ConversationProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
    }

    // 点击事件监听器
    private var onItemClickListener: ConversationActivity.OnItemClickListener? = null

    // 设置点击事件监听器
    fun setOnItemClickListener(onItemClickListener: ConversationActivity.OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
}
