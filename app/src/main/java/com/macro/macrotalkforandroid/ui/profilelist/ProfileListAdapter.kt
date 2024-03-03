package com.macro.macrotalkforandroid.ui.profilelist

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.R

// 学生资料列表适配器类
class ProfileListAdapter(
    private val context: Context,
    var profileList: List<Profile>,
    val isPrefab: Boolean
) :
    RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>() {

    // 创建ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    // 绑定数据到ViewHolder
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val data: Profile = profileList[position]
        // 加载学生头像
        if (!data.Images[0].isNotPrefab) {
            val input = context.assets.open(data.Images[0].ImageName + ".jpg")
            holder.profileListImage.setImageBitmap(BitmapFactory.decodeStream(input))
        } else {
            val bitmap = BitmapFactory.decodeFile(data.Images[0].ImageOriginalUri)
            holder.profileListImage.setImageBitmap(bitmap)
        }
        // 设置学生姓名和状态
        holder.profileListName.text = data.Name
        holder.profileListMomotalkState.text = data.MomotalkState
        // 加载学校图标
        if (data.School != null) {
            try {
                val school = data.School.let { context.assets.open("$it.png") }
                holder.profileListSchoolImage.setImageBitmap(BitmapFactory.decodeStream(school))
            } catch (_: Exception) {}
            try {
                holder.profileListSchoolImage.setImageBitmap(BitmapFactory.decodeFile(data.School))
            } catch (_: Exception) {}
        }
    }

    // 获取列表项数量
    override fun getItemCount(): Int {
        return profileList.size
    }

    // 在视图附加到窗口时播放动画
    override fun onViewAttachedToWindow(holder: ProfileViewHolder) {
        super.onViewAttachedToWindow(holder)
        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    // 添加列表项
    fun addItem(profile: Profile) {
        profileList += profile
        notifyItemInserted(profileList.size - 1)
    }

    // 删除列表项
    fun removeItem(profile: Profile, index: Int) {
        profileList -= profile
        notifyItemRemoved(index)
    }

    // 替换列表项
    fun replaceItem(profile: Profile, index: Int) {
        val list = profileList.toMutableList()
        list[index] = profile
        profileList = list.toList()
        notifyItemChanged(index)
    }

    // ViewHolder类
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileListImage: ImageView
        val profileListName: TextView
        val profileListMomotalkState: TextView
        val profileListSchoolImage: ImageView
        val view: View

        init {
            // 初始化控件
            profileListImage = itemView.findViewById(R.id.profilelist_Image)
            profileListName = itemView.findViewById(R.id.profilelist_Name)
            profileListMomotalkState = itemView.findViewById(R.id.profilelist_MomotalkState)
            profileListSchoolImage = itemView.findViewById(R.id.profilelist_SchoolImage)

            // 设置点击事件
            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, profileList[layoutPosition])
                }
            }

            // 如果不是预置项，设置长按事件
            if (!isPrefab) {
                itemView.setOnLongClickListener { v ->
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener!!.OnItemLongClick(v, profileList[layoutPosition])
                    }
                    true
                }
            }

            view = itemView
        }
    }

    // 点击事件接口
    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Profile?)
    }

    // 长按事件接口
    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, data: Profile?)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    // 设置点击事件监听器
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    // 设置长按事件监听器
    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}
