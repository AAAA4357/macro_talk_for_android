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


 // 会话头像适配器用于显示头像列表，并处理点击事件回调。

class ConversationAvatorAdapter(var images: List<Image>) :
    RecyclerView.Adapter<ConversationAvatorAdapter.ConversationAvatorViewHolder>() {

    lateinit var context: Context

    //创建ViewHolder并绑定布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationAvatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.conversation_viewpager_item, parent, false)
        return ConversationAvatorViewHolder(view)
    }

    //绑定ViewHolder的数据

    override fun onBindViewHolder(holder: ConversationAvatorViewHolder, position: Int) {
        holder.bindImage(images[position])
    }


      //返回列表项的数量
    override fun getItemCount(): Int {
        return images.size
    }


    //头像ViewHolder类
    inner class ConversationAvatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView

        init {
            itemImage = itemView.findViewById(R.id.item_image)

            // 设置点击事件监听器
            itemView.setOnClickListener { v ->
                onItemClickListener?.OnItemClick(v, layoutPosition)
            }
        }


        //绑定头像图片到ImageView

        fun bindImage(image: Image) {
            itemImage.setImageBitmap(
                if (!image.isNotPrefab) {
                    // 从资源中加载头像图片
                    BitmapFactory.decodeStream(context.assets.open(image.ImageName + ".jpg"))
                } else {
                    // 从文件中加载头像图片
                    BitmapFactory.decodeFile(image.ImageOriginalUri)
                }
            )
        }
    }

    // 点击事件监听器
    private var onItemClickListener: ConversationActivity.OnItemClickListener? = null


     //设置点击事件监听器

    fun setOnItemClickListener(onItemClickListener: ConversationActivity.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}
