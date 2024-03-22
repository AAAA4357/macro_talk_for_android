package com.macro.macrotalkforandroid.ui.profilelist.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.R

class ProfileAvatorAdapter()
    : RecyclerView.Adapter<ProfileAvatorAdapter.ProfileAvatorViewHolder>() {

    // 学生头像列表
    var avatorList: List<Image> = listOf()
    lateinit var context: Context

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAvatorViewHolder {
        // 从 XML 布局文件中创建 View
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_avator, parent, false)
        return ProfileAvatorViewHolder(view)
    }

    // 获取列表项数量
    override fun getItemCount(): Int {
        return avatorList.size
    }

    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: ProfileAvatorViewHolder, position: Int) {
        holder.bindImage(avatorList[position])
    }

    // 设置数据源并通知适配器数据变化
    fun setData(avators: List<Image>) {
        avatorList = avators
        notifyDataSetChanged()
    }

    inner class ProfileAvatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView

        init {
            profileImage = itemView.findViewById(R.id.profile_avator)
        }

        // 绑定图片到 ImageView
        fun bindImage(avator: Image) {
            if (!avator.isNotPrefab) {
                // 如果图片为预设的，则从 assets 文件夹加载图片
                val input = context.assets.open(avator.ImageName + ".jpg")
                profileImage.setImageBitmap(BitmapFactory.decodeStream(input))
            } else {
                // 否则从文件路径加载图片
                profileImage.setImageBitmap(BitmapFactory.decodeFile(avator.ImageOriginalUri))
            }
        }
    }
}
